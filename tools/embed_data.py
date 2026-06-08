#!/usr/bin/env python3
"""Embed callgraph.json into callgraph_viewer.html as EMBEDDED_GRAPH_DATA."""
import json

json_path = "tools/output/callgraph.json"
html_path = "tools/output/callgraph_viewer.html"

with open(json_path, "r", encoding="utf-8") as f:
    data = json.load(f)

with open(html_path, "r", encoding="utf-8") as f:
    html = f.read()

embed = (
    "var EMBEDDED_GRAPH_DATA = "
    + json.dumps(data, ensure_ascii=False)
    + ";\n\nvar graphData = null;"
)
html = html.replace("var graphData = null;", embed)

old_load = """function loadData() {
    var xhr = new XMLHttpRequest();
    try {
        xhr.open('GET', 'callgraph.json', false);
        xhr.send();
        if (xhr.status === 200) return JSON.parse(xhr.responseText);
    } catch(e) {}
    var input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = function() {
        var r = new FileReader();
        r.onload = function() { render(JSON.parse(r.result)); };
        r.readAsText(input.files[0]);
    };
    document.getElementById('graph-container').appendChild(input);
    document.getElementById('loading').textContent = 'Click to select callgraph.json';
    input.click();
    return null;
}"""

new_load = """function loadData() {
    if (typeof EMBEDDED_GRAPH_DATA !== 'undefined') {
        document.getElementById('debug').textContent = 'Using embedded data';
        return EMBEDDED_GRAPH_DATA;
    }
    var xhr = new XMLHttpRequest();
    try {
        xhr.open('GET', 'callgraph.json', false);
        xhr.send();
        if (xhr.status === 200) return JSON.parse(xhr.responseText);
    } catch(e) {}
    var input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = function() {
        var r = new FileReader();
        r.onload = function() { render(JSON.parse(r.result)); };
        r.readAsText(input.files[0]);
    };
    document.getElementById('graph-container').appendChild(input);
    document.getElementById('loading').textContent = 'Click to select callgraph.json';
    input.click();
    return null;
}"""

html = html.replace(old_load, new_load)

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html)

print(
    f"Embedded {len(data['nodes'])} nodes and {len(data['links'])} links into HTML"
)
