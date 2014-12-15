package perseus;

public class CreatePlot {
	
//	public enum Type { DISTRIBUTION, HEATMAP };
	
	private String inputFile;
	private String title;
	private String xlabel;
	private String ylabel;
	private String plotType;
	private String outputFile;
	
	/* constructor */
	public CreatePlot( String inputFile, String title, String xlabel, String ylabel, String plotType, String outputFile ){
		this.inputFile = inputFile;
		this.title = title;
		this.xlabel = xlabel;
		this.ylabel = ylabel;
		this.plotType = plotType;
		this.outputFile = outputFile;
	}
	
	
	public void callGnuplot( ){
		
		SystemCall sc;
		
		if ( plotType.equalsIgnoreCase("distribution") ){
			sc = new SystemCall("bash create_distribution_plots.bash "  + inputFile  + " "  + title + " " +  xlabel + " " + ylabel + " " + outputFile);
			sc.execute();
		}
		else if ( plotType.equalsIgnoreCase("heatmap") ){
		
		}
			
		
	}
	
}
