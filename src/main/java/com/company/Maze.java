package com.company;

import org.jetbrains.annotations.Contract;

import javax.json.Json;
import javax.json.JsonObject;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Maze {
    List<List<Point>> lines = new ArrayList<>();
    Set<Point> visited = new HashSet<>();
    Set<Integer> polygons = new HashSet<>();
    List<List<Point3D>> triangleStrips = new ArrayList<>();
    List<List<Boolean>> m;
    int wallHeight = 10;

    List<List<Point3D>> getTriangleStrips() {
        if (triangleStrips.size() > 0)
            return triangleStrips;
        return triangleStrips = lines.stream().map(line -> line.stream().<Point3D>mapMulti((e, consumer) -> {
                    consumer.accept(new Point3D(e.y, 0, e.x));
                    consumer.accept(new Point3D(e.y, wallHeight, e.x));
                }
        ).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public Maze(List<List<Boolean>> m) {
        this.m = m;
    }

    @Contract(pure = true)
    List<Point> neighborCoords(Point coord, int rowSize, int columnSize) {
        List<Point> res = new ArrayList<>();
        if (coord == null) {
            return res;
        }
        var r = coord.x;
        var c = coord.y;
        for (var rNew = r - 1; rNew <= r + 1; rNew++) {
            for (var cNew = c - 1; cNew <= c + 1; cNew++) {
                if (rNew >= 0 && cNew >= 0 && rNew < rowSize && cNew < columnSize && (rNew != r || cNew != c)) {
                    res.add(new Point(rNew, cNew));
                }
            }
        }
        assert res.size() < 9;
        assert res.stream().allMatch(p -> Math.sqrt(Math.pow(p.x - coord.x, 2) + Math.pow(p.y - coord.y, 2)) < 2);
        return res;
    }

    List<Point> nextNeighborCoords(Point coord) {
        List<Point> list = new ArrayList<>();
        var rowSize = m.size();
        var columnSize = m.size() == 0 ? 0 : m.get(0).size();
        var nCoords = neighborCoords(coord, rowSize, columnSize);
        assert nCoords.stream().allMatch(p -> neighborCoords(p, rowSize, columnSize).contains(coord));

        for (Point p : nCoords) {
            if (m.get(p.x).get(p.y) && !visited.contains(p)) {
                list.add(p);
            }
        }
        assert list.size() < 9;
        return list;
    }

    boolean addable(Point coord) {
        return coord != null && m.get(coord.x).get(coord.y) && !visited.contains(coord);
    }

    void findLineCoordLists() {
        var rowSize = m.size();
        var columnSize = m.size() == 0 ? 0 : m.get(0).size();
        for (var r = 0; r < rowSize; r++) {
            for (var c = 0; c < columnSize; c++) {
                var coord = new Point(r, c);
                if (!addable(coord)) {
                    continue;
                }

                var firstRun = true;
                Point otherNeighbor = null;
                while (addable(coord)) {
                    if (firstRun) {
                        lines.add(new ArrayList<>());
                        var nCoords = nextNeighborCoords(coord);
                        otherNeighbor = nCoords.size() > 0 ? nCoords.get(0) : null;
                        firstRun = false;
                    }
                    lines.get(lines.size() - 1).add(coord);
                    visited.add(coord);
                    if (coord == otherNeighbor) {
                        polygons.add(lines.size() - 1);
                    }
                    var nCoords = nextNeighborCoords(coord);
                    assert nCoords.stream().allMatch(Objects::nonNull);
                    coord = nCoords.size() > 0 ? nCoords.get(0) : null;
                }
                assert lines.stream().allMatch(Objects::nonNull);
                while (addable(otherNeighbor)) {
                    lines.get(lines.size() - 1).add(0, otherNeighbor);
                    visited.add(otherNeighbor);
                    var otherNeighbors = nextNeighborCoords(otherNeighbor);
                    otherNeighbor = otherNeighbors.size() > 0 ? nextNeighborCoords(otherNeighbor).get(0) : null;
                }
            }
        }
        assert lines.stream().reduce(0L, (acc, x) -> acc + x.size(), Long::sum).equals(m.stream().reduce(0L, (Long acc, List<Boolean> x) -> acc + x.stream().filter(y -> y).count(), Long::sum));
    }

    @Contract(mutates = "this")
    void compressLines() {
        lines = lines.stream().map(this::endpoints).collect(Collectors.toList());
    }

    @Contract(pure = true)
    List<Point> endpoints(List<Point> coords) {
        if (coords.size() <= 1) {
            return coords;
        }

        var dirPrev = new Point(coords.get(1).x - coords.get(0).x, coords.get(1).y - coords.get(0).y);
        Point dir = new Point(-1, -1); // Initialize to invalid value for value comparison
        List<List<Point>> chunks = new ArrayList<>();
        List<Point> chunk = new ArrayList<>();
        for (var i = 0; i < coords.size(); i++) {
            if (i < coords.size() - 1) {
                dir = new Point(coords.get(i + 1).x - coords.get(i).x, coords.get(i + 1).y - coords.get(i).y);
                assert List.of(-1, 0, 1).contains(dir.x) && List.of(-1, 0, 1).contains(dir.y);
            }
            chunk.add(coords.get(i));
            if (i == coords.size() - 1 || dir != dirPrev) {
                chunks.add(chunk);
                chunk = new ArrayList<>();
            }
            dirPrev = dir;
        }
        assert chunks.size() <= coords.size();
        assert chunks.stream().mapToLong(Collection::size).sum() == coords.size();
        return chunks.stream().flatMap(xs -> xs.size() == 1 ? xs.stream() : List.of(xs.get(0), xs.get(xs.size() - 1)).stream()).collect(Collectors.toList());
    }

    String toSvg() {
        var res = new StringBuilder();
        res.append(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">", m.get(0).size(), m.size()));
        for (var i = 0; i < lines.size(); i++) {
            var elemName = polygons.contains(i) ? "polygon" : "polyline";

            var pointsStr = new StringBuilder();
            for (int j = 0; j < lines.get(i).size(); j++) {
                pointsStr.append(String.format("%d,%d", lines.get(i).get(j).y, lines.get(i).get(j).x));
                if (j < lines.get(i).size() - 1) {
                    pointsStr.append(' ');
                }
            }
            res.append(String.format("<%s fill=\"none\" stroke=\"black\" points=\"%s\"/>", elemName, pointsStr));
        }
        res.append("</svg>");
        return res.toString();
    }

    String toJson() {
        List<MeshData> objects = new ArrayList<>();
        for (var i = 0; i < getTriangleStrips().size(); i++) {
            objects.add(new UshortData(Target.ElementArrayBuffer, IntStream.range(0, getTriangleStrips().get(i).size()).mapToObj(e -> (short) e).collect(Collectors.toList())));
            objects.add(new FloatVec3Data(Target.ArrayBuffer, getTriangleStrips().get(i).stream().map(e -> new float[]{e.x(), e.y(), e.z()}).collect(Collectors.toList())));
        }
        assert objects.size() == 2 * getTriangleStrips().size();
        var buf = new MeshBuffer(objects);
        assert buf.objects().size() % 2 == 0;
        assert buf.objects().get(0).count() > 0;
        assert buf.objects().get(1).count() > 0;
        var bufferViewsBuilder = Json.createArrayBuilder();
        int byteOffset = 0;
        var realLengths = buf.toBytesHelper().stream().map(List::size).collect(Collectors.toList());
        for (int i = 0; i < objects.size(); i++) {
            MeshData x = objects.get(i);
            bufferViewsBuilder.add(Json.createObjectBuilder()
                    .add("buffer", 0)
                    .add("byteOffset", byteOffset)
                    .add("byteLength", x.toBytes().length)
                    .add("target", x.target().value)
            );
            byteOffset += realLengths.get(i);
        }

        var primitivesBuilder = Json.createArrayBuilder();
        List<List<Point3D>> strips = getTriangleStrips();
        for (int i = 0; i < strips.size(); i++) {
            primitivesBuilder.add(Json.createObjectBuilder()
                    .add("attributes", Json.createObjectBuilder()
                            .add("POSITION", i * 2 + 1)
                    )
                    .add("indices", i * 2)
                    .add("mode", Mode.TriangleStrip.value)
            );
        }

        var accessorsBuilder = Json.createArrayBuilder();
        for (int i = 0; i < objects.size(); i++) {
            var maxBuilder = Json.createArrayBuilder();
            for (var x : objects.get(i).getMax()) {
                maxBuilder.add(x);
            }
            var minBuilder = Json.createArrayBuilder();
            for (var x : objects.get(i).getMin()) {
                minBuilder.add(x);
            }
            accessorsBuilder.add(Json.createObjectBuilder()
                    .add("bufferView", i)
                    .add("byteOffset", 0)
                    .add("componentType", objects.get(i).getComponentType())
                    .add("count", objects.get(i).count())
                    .add("type", objects.get(i).type())
                    .add("max",  maxBuilder)
                    .add("min", minBuilder)
            );
        }
        JsonObject model = Json.createObjectBuilder()
                .add("scenes", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("nodes", Json.createArrayBuilder()
                                        .add(0)
                                )
                        )
                )
                .add("nodes", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("mesh", 0)
                        )
                )
                .add("meshes", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("primitives", primitivesBuilder)
                        )
                )
                .add("buffers", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("uri", buf.toString())
                                .add("byteLength", buf.toBytes().length)
                        )
                )
                .add("bufferViews", bufferViewsBuilder)
                .add("accessors", accessorsBuilder)
                .add("asset", Json.createObjectBuilder()
                        .add("version", "2.0")
                )
                .build();
        return model.toString();
    }


    @Contract(pure = true)
    public static void main(String[] args) throws IOException {
        var w = 224;
        List<List<Boolean>> m = new ArrayList<>();
        try (var br = new BufferedReader(new FileReader("pacman.pbm"))) {
            String line;
            var sb = new StringBuilder();
            for (var i = 0; (line = br.readLine()) != null; i++) {
                if (i < 3) {
                    continue;
                }
                sb.append(line);
            }
            var arr = sb.toString().toCharArray();
            for (var i = 0; i < arr.length; i++) {
                if (i % w == 0) {
                    m.add(new ArrayList<>());
                }
                m.get(m.size() - 1).add(arr[i] == '1');
            }
//            assert m.stream().flatMap(Collection::stream).map(x -> x ? '1' : '0').equals(Arrays.stream(arr));


        }
        var maze = new Maze(m);
        maze.findLineCoordLists();
        maze.compressLines();
        var res = maze.toJson();
        try (var br = new BufferedWriter(new FileWriter("/tmp/got.gltf"))) {
            br.write(res);
        }
    }
}
