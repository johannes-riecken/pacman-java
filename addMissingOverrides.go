package main

import (
	"bufio"
	_ "embed"
	"encoding/json"
	"log"
	"os"
	"strings"
)

func main() {
	err := mainAux()
	if err != nil {
		log.Fatal(err)
	}
}

type region struct {
	startLine   int
	startColumn int
	endColumn   int
}

func mainAux() error {
	sarif, err := readSarif()
	if err != nil {
		return err
	}
	regions := getRegions(sarif)
	// traverse regions backwards
	for uri, fileRegions := range regions {
		for i := len(fileRegions) - 1; i >= 0; i-- {
			r := fileRegions[i]
			err := addOverrideAnnotation(uri, r)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

// copy the file under fileName to a temporary file
// open the temporary file for reading and a new file under uri for writing
// loop over the file and write the lines to the new file
// if the line matches the region's startLine, then first write "@Override\n"
// then write the line
func addOverrideAnnotation(fileName string, r region) error {
	// copy the file under fileName to fileName.tmp
	err := os.Rename(fileName, fileName+".tmp")
	if err != nil {
		return err
	}
	// open the temporary file for reading and a new file under uri for writing
	f, err := os.Open(fileName + ".tmp")
	if err != nil {
		return err
	}
	defer func(f *os.File) {
		_ = f.Close()
	}(f)
	w, err := os.Create(fileName)
	if err != nil {
		return err
	}
	defer func(w *os.File) {
		_ = w.Close()
	}(w)
	// loop over the file and write the lines to the new file
	// if the line number matches the region's startLine number, then first write "@override\n"
	// then write the line
	scanner := bufio.NewScanner(f)
	for i := 0; scanner.Scan(); i++ {
		lineText := scanner.Text()
		if i == r.startLine-1 {
			indentText := lineText[:len(lineText)-len(strings.TrimLeft(lineText, " "))]
			s := indentText + "@Override\n"
			// initialize indentText with the text of lineText's leading spaces

			_, err := w.WriteString(s)
			if err != nil {
				return err
			}
		}
		_, err := w.WriteString(lineText + "\n")
		if err != nil {
			return err
		}
	}
	return nil
}

func getRegions(sarif map[string]interface{}) map[string][]region {
	// read all the regions under the json path .runs[]|.results[]|.locations[]|.physicalLocation.region
	regions := make(map[string][]region)
	for _, run := range sarif["runs"].([]interface{}) {
		for _, result := range run.(map[string]interface{})["results"].([]interface{}) {
			for _, location := range result.(map[string]interface{})["locations"].([]interface{}) {
				physicalLocation := location.(map[string]interface{})["physicalLocation"].(map[string]interface{})
				regionUntyped := physicalLocation["region"].(map[string]interface{})
				regionTyped := region{
					startLine:   int(regionUntyped["startLine"].(float64)),
					startColumn: int(regionUntyped["startColumn"].(float64)),
					endColumn:   int(regionUntyped["endColumn"].(float64)),
				}
				uri := physicalLocation["artifactLocation"].(map[string]interface{})["uri"].(string)
				if regions[uri] == nil {
					regions[uri] = make([]region, 0)
				}
				regions[uri] = append(regions[uri], regionTyped)

			}
		}
	}
	return regions
}

// missingOverridesJSON is generated from the output of the following commands:
// codeql database create testdb --language go
// codeql database analyze testdb ~/.codeql/packages/codeql/java-queries/0.3.2/Advisory/Declarations/MissingOverrideAnnotation.ql --format=sarif-latest --output=missing_overrides.json
//go:embed missing_overrides.json
var missingOverridesJSON string

// readSarif unmarshals missing_overrides.json into a map[string]interface{}
func readSarif() (map[string]interface{}, error) {
	var sarif map[string]interface{}
	err := json.Unmarshal([]byte(missingOverridesJSON), &sarif)
	if err != nil {
		return nil, err
	}
	return sarif, nil
}
