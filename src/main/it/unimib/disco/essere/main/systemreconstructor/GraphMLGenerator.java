//package it.unimib.disco.essere.main.systemreconstructor;
//
//public class GraphMLGenerator {
//
//    private static final String RN = "\r\n";
//
//    private String getGraphMLHeader() {
//        String header = "<?xml version=\"1.0\" ?>";
//        header += "\r\n<graphml\r\n  xmlns=\"http://graphml.graphdrawing.org/xmlns\"\r\n  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n  xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd\"\r\n>";
//        header += "\r\n  <graph id=\"G\" edgedefault=\"directed\">";
//        return header;
//    }
//    
////    private String getAttributes(){
////        
////    }
//    
//    private String addAttribute(String attributes, String attribute String id, String attributeName, String attributeType){
//        attributes += RN + "  <key for=\"node\" id=\""+ id + "\" attr.name=\""+ attributeName + "\" attr.type=\""+ attributeType + "\" />";
//        return attributes;
//    }
//
////    <key id="A" for="node" attr.name="A" attr.type="double"></key>
////    <key id="CA" for="node" attr.name="CA" attr.type="int"></key>
////    <key id="Weight" for="edge" attr.name="Weight" attr.type="int"></key>
////    <key id="labelE" for="edge" attr.name="labelE" attr.type="string"></key>
//    
//    private String getGraphMLFooter() {
//        String footer = "\r\n  </graph>\r\n</graphml>";
//        return footer;
//    }
//    
//    private String getNode(String id) {
//        String node = "\r\n    <node id=\"" + id + "\">";
//        node += "\r\n      <data key=\"d5\"/>";
//        node += "\r\n      <data key=\"d6\">";
//        node += "\r\n        <y:ShapeNode>";
//        node += "\r\n          <y:NodeLabel>" + id + "</y:NodeLabel>";
//        node += "\r\n          <y:Shape type=\"rectangle\"/>";
//        node += "\r\n        </y:ShapeNode>";
//        node += "\r\n      </data>";
//        node += "\r\n    </node>";
//        return node;
//      }
//     
//      private String getEdge(Edge edge) {
//        Vertex source = edge.getOutVertex();
//        Vertex target = edge.getInVertex();
//        String edgeId = (String) edge.getId();
//        String sourceId = (String) source.getId();
//        String targetId = (String) target.getId();
//        String label = edge.getLabel();
//     
//        String edgeXml = "\r\n    <edge id=\"" + edgeId + "\" source=\"" + sourceId + "\" target=\"" + targetId + "\" label=\"" + label + "\">";
//        edgeXml += "\r\n    </edge>";
//     
//        return edgeXml;
//      }
//    
//    
//    private void createGraphXml() {
//        xml = getGraphMLHeader();
//     
//        // Create nodes
//        Iterable<Vertex> vertices = graph.getVertices();
//        Iterator<Vertex> verticesIterator = vertices.iterator();
//        while (verticesIterator.hasNext()) {
//          Vertex vertex = verticesIterator.next();
//          String id = (String) vertex.getId();
//          String node = getNode(id);
//          xml += node;
//        }
//        // Create edges
//        Iterable<Edge> edges = graph.getEdges();
//        Iterator<Edge> edgesIterator = edges.iterator();
//        while (edgesIterator.hasNext()) {
//          Edge edge = edgesIterator.next();
//          String edgeXml = getEdge(edge);
//          xml += edgeXml;
//        }
//     
//        xml += getGraphMLFooter();
//      }
//     
//      public void outputGraph(final OutputStream out) throws IOException {
//        createGraphXml();
//        System.out.println(xml);
//        try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out))) {
//          br.write(xml);
//          br.flush();
//        }
//        out.flush();
//        out.close();
//      }
//}
