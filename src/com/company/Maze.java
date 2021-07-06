package com.company;

import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Maze {
    List<List<Point>> lines = new ArrayList<>();
    Set<Point> visited = new HashSet<>();
    Set<Integer> polygons = new HashSet<>();
    List<List<Boolean>> m;

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
                    lines.get(lines.size()-1).add(coord);
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
                m.get(m.size()-1).add(arr[i] == '1');
            }
//            assert m.stream().flatMap(Collection::stream).map(x -> x ? '1' : '0').equals(Arrays.stream(arr));


        }
        var maze = new Maze(m);
        maze.findLineCoordLists();
        maze.compressLines();
        System.out.println(maze.toSvg());
    }
}
