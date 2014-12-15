import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;

public class InteractiveExploration {
	
	Connection connection = null;
	Statement statement = null;
	Graph graph;
	Graph collapsedGraph;
	GraphCollapser collapser;

	Layout layout;
	VisualizationViewer<Integer, String> vv;
	String tableName = "";

	int numNbrsToShow = 1;

	public InteractiveExploration(String dbPath, String tableName)
			throws ClassNotFoundException, IOException {
		this.tableName = tableName;
		Class.forName("org.sqlite.JDBC");
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			DatabaseMetaData dbm = connection.getMetaData();
			// check if table exists
			ResultSet tables = dbm.getTables(null, null, tableName, new String[] {"TABLE"});			
			if (!tables.next()) {
				System.out.println("Current directory = " + System.getProperty("user.dir"));
				System.out.println("Table "+tableName+ " does not exist in the database. Checked the database "+dbPath + ". Exiting...");
				System.exit(-1);
			}
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	// a edge name to insert into graph. The name is unused anywhere as of now
	private String createEdgeName(int id, int nbr) {
		return Integer.toString(id) + " " + Integer.toString(nbr);
	}

	public void visualizeGraph(ArrayList<Integer> seedNodes) {
		graph = new DirectedSparseGraph<Integer, String>();
		for (int node : seedNodes)
			graph.addVertex(node);
		collapsedGraph = graph;
		collapser = new GraphCollapser(graph);

		layout = new SpringLayout<Integer, String>(graph);
		layout.setSize(new Dimension(600, 600));
		vv = new VisualizationViewer<Integer, String>(layout);
		/* vv.getRenderContext().setVertexShapeTransformer(
				new ClusterVertexShapeFunction()); */
		vv.setPreferredSize(new Dimension(600, 600));

		Transformer<String, Double> edgeWeights = new Transformer<String, Double>() {
			@Override
			public Double transform(String edge) {
				String[] ends = edge.split(" ");
				try {
					ResultSet rs = statement.executeQuery("select weight from "
							+ tableName + " where source=" + ends[0]
							+ "and destination=" + ends[1]);
					rs.next();
					return rs.getDouble("weight");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return -1.0;
			}
		};
		
		DefaultModalGraphMouse<Integer, String> gm = new DefaultModalGraphMouse<Integer, String>();
		vv.addKeyListener(gm.getModeKeyListener());
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(gm);

		
		
		final PickedState<Integer> ps = vv.getPickedVertexState();
		ps.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object subject = e.getItem();
				if (subject instanceof Integer) {
					Integer pickedNode = (Integer) subject;
					if (ps.isPicked(pickedNode)) {
						/* generateGraph(node); */

						// get neighbors of the node from database
						assert connection != null;
						try {
							Collection collapseThese = new HashSet<Integer>();
							// get neighbors to which it points (directed graph
							// assumption)
							ResultSet rs = statement
									.executeQuery("select destination,weight from "
											+ tableName
											+ " where source="
											+ Integer.toString(pickedNode)
											+ " order by weight desc");

							Graph inGraph = layout.getGraph();
							int count = 0;
							while (rs.next()) {
								int dest = rs.getInt("destination");
								double weight = rs.getDouble("weight");
								if (count >= numNbrsToShow)
									collapseThese.add(dest);
								inGraph.addEdge(
										createEdgeName(pickedNode, dest),
										pickedNode, dest);
								count++;
							}

							Graph clusterGraph = collapser.getClusterGraph(
									inGraph, collapseThese);
							Graph newg = collapser.collapse(inGraph,
									clusterGraph);
							collapsedGraph = newg;

							double sumx = 0;
							double sumy = 0;
							for (Object v : collapseThese) {
								Point2D p = (Point2D) layout.transform(v);
								sumx += p.getX();
								sumy += p.getY();
							}
							Point2D cp = new Point2D.Double(sumx
									/ collapseThese.size(), sumy
									/ collapseThese.size());

							vv.getRenderContext()
									.getParallelEdgeIndexFunction().reset();
							layout.setGraph(newg);
							layout.setLocation(clusterGraph, cp);
							vv.getPickedVertexState().clear();
							vv.repaint();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		Transformer<Integer, Paint> vertexColor = new Transformer<Integer, Paint>() {
			public Paint transform(Integer n) {
				return Color.BLUE;
			}
		};

		Transformer<Integer, String> vertexLabel = new Transformer<Integer, String>() {
			public String transform(Integer n) {
				return Integer.toString(n);
			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		vv.getRenderContext().setVertexLabelTransformer(vertexLabel);

		JFrame frame;
		frame = new JFrame("Graph Visualization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);

		/*
		 * try { if (connection != null) connection.close(); } catch
		 * (SQLException e) { System.err.println(e); } }
		 */
	}

	public void vizGraph(ArrayList<String> seedNodes) {

		graph = new DirectedSparseGraph<Integer, String>();
		for (String node : seedNodes)
			graph.addVertex(node);
		collapsedGraph = graph;
		collapser = new GraphCollapser(graph);

		layout = new SpringLayout<Integer, String>(graph);
		layout.setSize(new Dimension(600, 600));
		vv = new VisualizationViewer<Integer, String>(layout);
		/* vv.getRenderContext().setVertexShapeTransformer(
				new ClusterVertexShapeFunction()); */
		vv.setPreferredSize(new Dimension(600, 600));

		Transformer<String, Double> edgeWeights = new Transformer<String, Double>() {
			@Override
			public Double transform(String edge) {
				String[] ends = edge.split(" ");
				try {
					ResultSet rs = statement.executeQuery("select weight from "
							+ tableName + " where source=" + ends[0]
							+ "and destination=" + ends[1]);
					rs.next();
					return rs.getDouble("weight");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return -1.0;
			}
		};
		
		DefaultModalGraphMouse<Integer, String> gm = new DefaultModalGraphMouse<Integer, String>();
		vv.addKeyListener(gm.getModeKeyListener());
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(gm);

		
		
		final PickedState<Integer> ps = vv.getPickedVertexState();
		ps.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object subject = e.getItem();
				if (subject instanceof Integer) {
					Integer pickedNode = (Integer) subject;
					if (ps.isPicked(pickedNode)) {
						/* generateGraph(node); */

						// get neighbors of the node from database
						assert connection != null;
						try {
							Collection collapseThese = new HashSet<Integer>();
							// get neighbors to which it points (directed graph
							// assumption)
							ResultSet rs = statement
									.executeQuery("select destination,weight from "
											+ tableName
											+ " where source="
											+ Integer.toString(pickedNode)
											+ " order by weight desc");

							Graph inGraph = layout.getGraph();
							int count = 0;
							while (rs.next()) {
								int dest = rs.getInt("destination");
								double weight = rs.getDouble("weight");
								if (count >= numNbrsToShow)
									collapseThese.add(dest);
								inGraph.addEdge(
										createEdgeName(pickedNode, dest),
										pickedNode, dest);
								count++;
							}

							Graph clusterGraph = collapser.getClusterGraph(
									inGraph, collapseThese);
							Graph newg = collapser.collapse(inGraph,
									clusterGraph);
							collapsedGraph = newg;

							double sumx = 0;
							double sumy = 0;
							for (Object v : collapseThese) {
								Point2D p = (Point2D) layout.transform(v);
								sumx += p.getX();
								sumy += p.getY();
							}
							Point2D cp = new Point2D.Double(sumx
									/ collapseThese.size(), sumy
									/ collapseThese.size());

							vv.getRenderContext()
									.getParallelEdgeIndexFunction().reset();
							layout.setGraph(newg);
							layout.setLocation(clusterGraph, cp);
							vv.getPickedVertexState().clear();
							vv.repaint();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		Transformer<Integer, Paint> vertexColor = new Transformer<Integer, Paint>() {
			public Paint transform(Integer n) {
				return Color.BLUE;
			}
		};

		Transformer<Integer, String> vertexLabel = new Transformer<Integer, String>() {
			public String transform(Integer n) {
				return Integer.toString(n);
			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		vv.getRenderContext().setVertexLabelTransformer(vertexLabel);

		JFrame frame;
		frame = new JFrame("Graph Visualization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		
	}
}
