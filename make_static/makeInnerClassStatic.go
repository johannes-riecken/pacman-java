// TODO: This requires more refactoring than just adding a static, because the methods are not static which is not allowed.
package main

import (
	"bufio"
	_ "embed"
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
)

type region struct {
	startLine   int
	startColumn int
	endColumn   int
}

func main() {
	err := mainAuxStaticClass()
	if err != nil {
		log.Fatal(err)
	}
}

func mainAuxStaticClass() error {
	// staticClassJSON is generated from the output of the following commands:
	// codeql database create testdb --language java
	// codeql database analyze testdb ~/.codeql/packages/codeql/java-queries/0.3.2/Advisory/Declarations/MissingOverrideAnnotation.ql --format=sarif-latest --output=missing_overrides.json
	staticClassJSON, err := ioutil.ReadFile("inner_class_static.json")
	if err != nil {
		return err
	}

	sarif, err := readSarif(staticClassJSON)
	if err != nil {
		return err
	}
	regions := getRegions(sarif)
	// traverse regions backwards
	for uri, fileRegions := range regions {
		for i := len(fileRegions) - 1; i >= 0; i-- {
			r := fileRegions[i]
			err := makeInnerClassStatic(uri, r)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

func makeInnerClassStatic(fileName string, r region) error {
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
			skip := "class "
			j := r.startColumn - 1 - len(skip)
			lineText = lineText[:j] + "static " + lineText[j:]
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

// readSarif unmarshals missing_overrides.json into a map[string]interface{}
func readSarif(contents []byte) (map[string]interface{}, error) {
	var sarif map[string]interface{}
	err := json.Unmarshal(contents, &sarif)
	if err != nil {
		return nil, err
	}
	return sarif, nil
}
