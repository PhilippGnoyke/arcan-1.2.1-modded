package it.unimib.disco.essere.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;

public class JavaCompileGraphBuildingUtils {

	private OutputDirUtils outputDirUtils;

	public JavaCompileGraphBuildingUtils(OutputDirUtils outputDirUtils)
	{
		this.outputDirUtils = outputDirUtils;
	}

	static final Logger logger = LogManager.getLogger(JavaCompileGraphBuildingUtils.class);
	public void multithreadCompileGitNoMaven(File projectFolder) {
		ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(8));
		Stack<ListenableFuture<Graph>> futureList = new Stack<>();
		for (File d : projectFolder.listFiles()) {
			File graphML = Paths.get(d.getAbsolutePath(), "sytem.graphml").toFile();
			logger.debug("Project: " + d + " file: " + graphML.getName() + " exist?" + graphML.exists());
			if (!graphML.exists() && d.exists() && d.isDirectory()) {
				logger.debug("Founded project: " + d + " file: " + graphML.getName() + " exist?"
						+ graphML.exists());
				ListenableFuture<Graph> g = runJavaCompileThreadsAndWriteCSVandGraphml(pool, d, graphML);
				futureList.add(g);
			}
		}
		// TODO close all thread after they have finished
		pool.shutdown();// FIXME wait for all thread termination
	}


	private ListenableFuture<Graph> runJavaCompileThreadsAndWriteCSVandGraphml(ListeningExecutorService pool, File d,
			File graphML) {
		final ListenableFuture<Graph> future = createGraphFuture(pool, d);
		Futures.addCallback(future, new FutureCallback<Graph>() {
			@Override
			public void onSuccess(Graph graph) {
				Map<String, List<String>> smellMap;
				try {
					logger.debug("***Metrics*** " + graph);
					String s[] = graph.toString().split(" ");
					String vertices = s[0].split("vertices:")[1];
					String edges = s[1].split("edges:")[1];
					edges = edges.substring(0, edges.length() - 1);
					if (!"0".equals(vertices)) {
						PackageMetricsCalculator metricsCalculator = new PackageMetricsCalculator(graph);
						logger.debug("***Unstable dependencies start***");
						UnstableDependencyDetector unstableDependencyDetector = new UnstableDependencyDetector(graph,
								metricsCalculator);

						smellMap = unstableDependencyDetector.detect();
						Map<String, Double> instabilityMap = unstableDependencyDetector.getInstabilityMap();
						File csvFile = Paths.get(d.getAbsolutePath(), "UnstableDependencies.csv").toAbsolutePath()
								.toFile();
						logger.debug("***CSV initializated***");
						PrintWriter writer = new PrintWriter(csvFile);
						String[] header = { "UnstabeDependenciesPackage", "InstabilityUnstabeDependenciesPackage",
								"CorrelatedPackage", "InstabilityCorrelatedPackage", "system", "vertices", "edges" };
						CSVFormat formatter = CSVFormat.EXCEL.withHeader(header);
						CSVPrinter printer = new CSVPrinter(writer, formatter);

						for (Entry<String, List<String>> entry : smellMap.entrySet()) {
							for (String interestedPackage : entry.getValue()) {
								printer.print(entry.getKey());
								printer.print(instabilityMap.get(entry.getKey()));
								printer.print(interestedPackage);
								printer.print(instabilityMap.get(interestedPackage));
								printer.print(d.getName());
								printer.print(vertices);
								printer.print(edges);
								printer.println();
							}
						}
						printer.close();
						writer.close();
						printer = null;
						writer = null;

						OutputStream out = new FileOutputStream(graphML);
						graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(out, graph);
						try {
							if (graph != null) {
								graph.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.debug("***CSV written***");
					}
				} catch (TypeVertexException e) {
					logger.error(e.getMessage(), e.fillInStackTrace());
				} catch (IOException e) {
					logger.error(e.getMessage(), e.fillInStackTrace());
				}
				logger.debug("***End of Unstable dependecies detection*** " + graph);
			}

			@Override
			public void onFailure(Throwable throwable) {
				logger.error("Exception in task", throwable);
			}
		});
		return future;
	}

	private ListenableFuture<Graph> createGraphFuture(ListeningExecutorService pool, File directory) {
		return pool.submit(new Callable<Graph>() {
			@Override
			public Graph call() throws Exception {
				InterfaceModel model = new InterfaceModel(outputDirUtils);
				model.setProjectFolder(directory);
				model.buildGraphTinkerpop();
				logger.info("***Finished to build graph*** " + model.getGraph() + " - " + directory.getName());
				return model.getGraph();
			}
		});
	}

}
