package it.unimib.disco.essere.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import it.unimib.disco.essere.main.graphmanager.GraphBuilderAllHistorySystemByCommit;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.terminal.ParameterGitValueTerminal;
import it.unimib.disco.essere.main.terminal.ParametersNeo4jDBTerminal;

public class IXPDDetectionUtils {
	static final Logger logger = LogManager.getLogger(IXPDDetectionUtils.class);
	private OutputDirUtils outputDirUtils; // Modded

	public IXPDDetectionUtils(OutputDirUtils outputDirUtils)
	{
		this.outputDirUtils = outputDirUtils;
	}

	public Map<Long, Map<String, Long>> _commitModification = null;
	
	private static final String FILE_IMPLICIT_CROSS_PACKAGE_DEPENDENCY = "IXPD.csv";

	//	public IXPDDetectionUtils(Map<Long, Map<String, Long>> _commitModification) {
	//		this._commitModification = _commitModification;
	//	}

	public void printCommitHisory(File _gitFolder) throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		GraphBuilderAllHistorySystemByCommit j = new GraphBuilderAllHistorySystemByCommit(_gitFolder);
		Map<String, String> m = j.writeGraphByCommitOnlyFreecolNames();
		File p = Paths.get(_gitFolder.getParentFile().getAbsolutePath(), "SystemModificationCommitLong.csv")
				.toAbsolutePath().toFile();
		logger.info("***CSV initializated*** -exist " + p.exists() + " file: " + p.getName());
		PrintWriter writer = new PrintWriter(p);
		String[] header = { "Commit", "system" };
		CSVFormat formatter = CSVFormat.EXCEL.withHeader(header);
		CSVPrinter printer = new CSVPrinter(writer, formatter);
		for (String i : m.keySet()) {
			logger.info("commit: " + i + " " + m.get(i));
			printer.print(i);
			printer.print(m.get(i));
			printer.println();
		}
		printer.close();
		writer.close();
		printer = null;
		writer = null;
		logger.info("***CSV written***");
	}

	public Graph writeGraphHistoryAndDetectImplicitCrossPackageDependency(ParametersNeo4jDBTerminal  _parNeo4j, File _dbGitFolder, File _gitFolder, File _outDir)
			throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		GraphWriter graphW = null;
		Graph graph = null;
		if (_parNeo4j._writeNeo4j) {
			graphW = new Neo4JGraphWriter();
			graphW.setup(_dbGitFolder.toString());
			graph = graphW.init();
			logger.debug("Initialized Neo4j");
		} else {
			graph = TinkerGraph.open();
		}
		GraphBuilderAllHistorySystemByCommit j = new GraphBuilderAllHistorySystemByCommit(_gitFolder);
		// Map<RevCommit,Map<String, List<String>>> o =
		// j.writeGraphByCommitImplictCrossPackageDepedencyForEveryCommit(graph,
		// graphW);
		// if (_outDir != null) {
		// createOutputDir(_outDir);
		// File csv = _outDir;
		// printGitHistoryIXPDCSVforEveryCommit(o, csv);
		// } else {
		// _arcanSubfolder =
		// _gitFolder.getAbsoluteFile().getParentFile().getAbsolutePath() +
		// ARCAN_OUTPUT_URL;
		// createOutputDir(_arcanSubfolder);
		// File csv = Paths.get(_arcanSubfolder).toFile();
		// printGitHistoryIXPDCSVforEveryCommit(o, csv);
		// }
		Map<String, List<String>> o = j.writeGraphByCommitImplictCrossPackageDepedency(graph, graphW);
		if (_outDir != null) {
			outputDirUtils.createDir(_gitFolder.getAbsoluteFile().getParentFile(),false);
			//			createOutputDir(_outDir);
			File csv = _outDir;
			printGitHistoryIXPDCSV(o, csv);
		} else {
			//			_arcanSubfolder = _gitFolder.getAbsoluteFile().getParentFile().getAbsolutePath() + ARCAN_OUTPUT_URL;
			outputDirUtils.createDir(_gitFolder.getAbsoluteFile().getParentFile() , false);
			//			createOutputDir(_arcanSubfolder);
			//			File csv = Paths.get(_arcanSubfolder).toFile();
			File csv = outputDirUtils.getOutputFolder();
			printGitHistoryIXPDCSV(o, csv);
		}
		return graph;
	}

	public void printGitHistoryIXPDCSV(Map<String, List<String>> o, File csv)
			throws FileNotFoundException, IOException {
		logger.info("***CSV initializated*** -exist " + csv.exists() + " file: " + csv.getName()
		+ FILE_IMPLICIT_CROSS_PACKAGE_DEPENDENCY);
		PrintWriter writer = new PrintWriter(csv + File.separator + FILE_IMPLICIT_CROSS_PACKAGE_DEPENDENCY);
		/**
		 * 3 commit-out: 3, in: 4, ratio out: 1,000000, in: 0,750000;
		 * src.it.unimib.disco.essere.toysystem.olympusextension.Athena ->
		 * src.it.unimib.disco.essere.main.MetricsCalculator List<String> l =
		 * r.get(nameOutV); l.add(nameInV); l.add(""+o); l.add(""+i);
		 * l.add(""+ratioOut); l.add(""+ratioIn);
		 */
		String[] header = { "java-out", "java-in", "commit-out", "commit-in", "ratio-out", "ration-in", "totcounter" };
		CSVFormat formatter = CSVFormat.EXCEL.withHeader(header);
		CSVPrinter printer = new CSVPrinter(writer, formatter);
		for (String i : o.keySet()) {
			logger.info("commit: " + i + " " + o.get(i));

			if (o.get(i).isEmpty()) {
				logger.debug("***" + i + " is not a IXPD	***");
			} else {
				printer.print(i);// "java-out"
				for (String e : o.get(i)) {
					/**
					 * List<String> l = r.get(nameOutV); l.add(nameInV);
					 * l.add(""+o); l.add(""+i); l.add(""+ratioOut);
					 * l.add(""+ratioIn);
					 */
					printer.print(e);
				}
				printer.println();
			}
		}
		printer.close();
		writer.close();
		printer = null;
		writer = null;
		logger.info("***CSV written***");
	}

	public void printGitHistoryIXPDCSVforEveryCommit(Map<RevCommit, Map<String, List<String>>> o, File csv)
			throws FileNotFoundException, IOException {
		logger.info("***CSV initializated*** -exist " + csv.exists() + " file: " + csv.getName()
		+ FILE_IMPLICIT_CROSS_PACKAGE_DEPENDENCY);
		PrintWriter writer = new PrintWriter(csv + File.separator + FILE_IMPLICIT_CROSS_PACKAGE_DEPENDENCY);
		/**
		 * 3 commit-out: 3, in: 4, ratio out: 1,000000, in: 0,750000;
		 * src.it.unimib.disco.essere.toysystem.olympusextension.Athena ->
		 * src.it.unimib.disco.essere.main.MetricsCalculator List<String> l =
		 * r.get(nameOutV); l.add(nameInV); l.add(""+o); l.add(""+i);
		 * l.add(""+ratioOut); l.add(""+ratioIn);
		 */
		String[] header = { "version", "commit_time", "java-out", "java-in", "commit-out", "commit-in", "ratio-out",
				"ration-in", "totcounter" };
		CSVFormat formatter = CSVFormat.EXCEL.withHeader(header);
		CSVPrinter printer = new CSVPrinter(writer, formatter);
		for (RevCommit i : o.keySet()) {
			logger.info("version commit: " + i + " " + o.get(i));
			if (o.get(i).isEmpty()) {
				logger.debug("***" + i + " is not a IXPD	***");
			} else {

				for (String l : o.get(i).keySet()) {
					logger.info("commit: " + l + " " + o.get(i).get(l));
					if (o.get(i).get(l).isEmpty()) {
						logger.debug("***" + i + " is not a IXPD	***");
					} else {
						printer.print(i.getId().getName());// "version"
						printer.print(i.getCommitTime());// "commit_time"
						printer.print(l);// "java-out"
						for (String e : o.get(i).get(l)) {
							printer.print(e);
						}
						printer.println();
					}
				}
			}
		}
		printer.close();
		writer.close();
		printer = null;
		writer = null;
		logger.info("***CSV written***");
	}
}