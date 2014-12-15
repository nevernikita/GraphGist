import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.Animator;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;

@SuppressWarnings("serial")
public class VertexCollapse extends JApplet {
    
    Graph graph;
    Graph collapsedGraph;    
    VisualizationViewer vv;
    Layout layout;
    
    GraphCollapser collapser;
    public VertexCollapse() {
        
        // create a simple graph for the demo
        graph = 
            TestGraphs.getDemoGraph(); 
        collapsedGraph = graph;
        collapser = new GraphCollapser(graph);
        
        layout = new FRLayout(graph);

        Dimension preferredSize = new Dimension(400,400);
        final VisualizationModel visualizationModel = 
            new DefaultVisualizationModel(layout, preferredSize);
        vv =  new VisualizationViewer(visualizationModel, preferredSize);
        
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction());
        
        final PredicatedParallelEdgeIndexFunction eif = PredicatedParallelEdgeIndexFunction.getInstance();
        final Set exclusions = new HashSet();
        eif.setPredicate(new Predicate() {

			public boolean evaluate(Object e) {
				
				return exclusions.contains(e);
			}});
        
        
        vv.getRenderContext().setParallelEdgeIndexFunction(eif);

        vv.setBackground(Color.white);
        
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller() {			
			@Override
			public String transform(Object v) {
				if(v instanceof Graph) {
					return ((Graph)v).getVertices().toString();
				}
				return super.transform(v);
			}});
                
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);

        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        
        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        
        final ScalingControl scaler = new CrossoverScalingControl();
    
        JButton collapse = new JButton("Collapse");
        collapse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
                if(picked.size() > 1) {
                    Graph inGraph = layout.getGraph();
                    Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

                    Graph g = collapser.collapse(layout.getGraph(), clusterGraph);
                    collapsedGraph = g;
                    double sumx = 0;
                    double sumy = 0;
                    for(Object v : picked) {
                    	Point2D p = (Point2D)layout.transform(v);
                    	sumx += p.getX();
                    	sumy += p.getY();
                    }
                    Point2D cp = new Point2D.Double(sumx/picked.size(), sumy/picked.size());
                    vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                    layout.setGraph(g);
                    layout.setLocation(clusterGraph, cp);
                    vv.getPickedVertexState().clear();
                    vv.repaint();
                }
            }});
        
        
        JButton expand = new JButton("Expand");
        expand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
                for(Object v : picked) {
                    if(v instanceof Graph) {
                        
                        Graph g = collapser.expand(layout.getGraph(), (Graph)v);
                        vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                        layout.setGraph(g);
                    }
                    vv.getPickedVertexState().clear();
                   vv.repaint();
                }
            }});
                
        
        Class[] combos = getCombos();
        final JComboBox jcb = new JComboBox(combos);
        // use a renderer to shorten the layout name presentation
        jcb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        jcb.addActionListener(new LayoutChooser(jcb, vv));
        jcb.setSelectedItem(FRLayout.class);


        JPanel controls = new JPanel();               
        JPanel collapseControls = new JPanel(new GridLayout(3,1));
        collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
        collapseControls.add(collapse);
        collapseControls.add(expand);        
        controls.add(collapseControls);
        controls.add(modeBox);        
        controls.add(jcb);
        content.add(controls, BorderLayout.SOUTH);
    }
    
    class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {

        ClusterVertexShapeFunction() {
            setSizeTransformer(new ClusterVertexSizeFunction<V>(20));
        }
        @Override
        public Shape transform(V v) {
            if(v instanceof Graph) {
                int size = ((Graph)v).getVertexCount();
                if (size < 8) {   
                    int sides = Math.max(size, 3);
                    return factory.getRegularPolygon(v, sides);
                }
                else {
                    return factory.getRegularStar(v, size);
                }
            }
            return super.transform(v);
        }
    }
    
    class ClusterVertexSizeFunction<V> implements Transformer<V,Integer> {
    	int size;
        public ClusterVertexSizeFunction(Integer size) {
            this.size = size;
        }

        public Integer transform(V v) {
            if(v instanceof Graph) {
                return 30;
            }
            return size;
        }
    }

	private class LayoutChooser implements ActionListener
    {
        private final JComboBox jcb;
        private final VisualizationViewer vv;

        private LayoutChooser(JComboBox jcb, VisualizationViewer vv)
        {
            super();
            this.jcb = jcb;
            this.vv = vv;
        }

        public void actionPerformed(ActionEvent arg0)
        {
            Object[] constructorArgs =
                { collapsedGraph };

            Class<? extends Layout> layoutC = 
                (Class<? extends Layout>) jcb.getSelectedItem();
//            Class lay = layoutC;
            try
            {
                Constructor<? extends Layout> constructor = layoutC
                        .getConstructor(new Class[] {Graph.class});
                Object o = constructor.newInstance(constructorArgs);
                Layout l = (Layout) o;
                l.setInitializer(vv.getGraphLayout());
                l.setSize(vv.getSize());
                layout = l;
				LayoutTransition lt =
					new LayoutTransition(vv, vv.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.repaint();
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

	@SuppressWarnings("unchecked")
    private Class<? extends Layout>[] getCombos()
    {
        List<Class<? extends Layout>> layouts = new ArrayList<Class<? extends Layout>>();
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        layouts.add(SpringLayout2.class);
        layouts.add(ISOMLayout.class);
        return layouts.toArray(new Class[0]);
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new VertexCollapse());
        f.pack();
        f.setVisible(true);
    }
}


