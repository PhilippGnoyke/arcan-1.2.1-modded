package it.unimib.disco.essere.main.graphmanager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.gitblit.utils.JGitUtils;
import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.main.asengine.ImplicitCrossModuleDependencyDetector;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByCommit;

public class GraphBuilderAllHistorySystemByCommit {
	private static final Logger logger = LogManager.getLogger(GraphBuilderAllHistorySystemByCommit.class);

	File _gitFolder = null;

	public GraphBuilderAllHistorySystemByCommit(final File gitFolder) throws IsNotGitFolderException{
		if(wellFormedGitPath(gitFolder)){
			_gitFolder=gitFolder;
		}else{
			throw new IsNotGitFolderException(gitFolder);
		}
	}

	private boolean wellFormedGitPath(final File file){
		if(file!=null && file.isDirectory() && ".git".equals(file.getName())){
			return true;
		}else{
			return false;
		}
	}

	public Map<String, List<String>> writeGraphByCommitImplictCrossPackageDepedency(final Graph graph, final GraphWriter graphW) throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_gitFolder);
		logger.info("repodir: "+repoDir);
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(repoDir)
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build()) {
			logger.debug("Having repository: " + repository.getDirectory());
			// the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
			Ref head = repository.exactRef("refs/heads/master");
			logger.debug("Ref of refs/heads/master: " + head);

			try(Git git = new Git(repository)){
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> l = ImmutableList.copyOf(logs).reverse().iterator();
				int i = 0;
				RevCommit beforeCommit = null;
				while(l.hasNext()){
					RevCommit c = l.next();
					SystemBuilderByCommit sys = new SystemBuilderByCommit(repository,c);
					sys.readClass(null);
					GraphBuilderSystemByCommit graphB = new GraphBuilderSystemByCommit(sys.getJavas());
					graphB.createGraph(graph, beforeCommit, sys.getCommit());
					if(graphW!=null){
						graphW.write(graph, false);
						logger.debug(i+" Writed System");
					}else{
						logger.debug(i+" Saved System");
					}
					
					beforeCommit=c;
					i++;
				}
			}
			Map<String, List<String>> ixpd = new ImplicitCrossModuleDependencyDetector(graph).detect();
			if(graphW!=null){
				graphW.write(graph, false);
				logger.debug("Writed IXPD instances");
			}
			
			return ixpd;
		}
	}
	private void createOutputDir(String directoryName) {
		File theDir = new File(directoryName);
		createOutputDir(theDir);
	}
	private void createOutputDir(File theDir) {
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.debug("creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException e) {
				logger.error(e.getMessage());
			}
			if (result) {
				logger.debug("DIR created");
			}
		}
	}
	public Map<RevCommit, Map<String,List<String>>> writeGraphByCommitImplictCrossPackageDepedencyForEveryCommit(final Graph graph, final GraphWriter graphW) throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_gitFolder);
		String _arcanSubfolder = _gitFolder.getAbsoluteFile().getParentFile().getAbsolutePath() +File.separator + "ArcanOutput";
		createOutputDir(_arcanSubfolder);
		File csv = Paths.get(_arcanSubfolder).toFile();
		PrintWriter writer = new PrintWriter(csv + File.separator + "IXPD.csv");
		logger.info("csv file: " + csv.getAbsolutePath());
		String[] header = {"version","commit_time", "java-out", "java-in", "commit-out", "commit-in", "ratio-out", "ration-in", "totcounter" };
		CSVFormat formatter = CSVFormat.EXCEL.withHeader(header);
		CSVPrinter printer = new CSVPrinter(writer, formatter);
				
		logger.info("repodir: "+repoDir);
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(repoDir)
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build()) {
			Map<RevCommit,Map<String,List<String>>> historyIXPD = new HashMap<RevCommit,Map<String, List<String>>>();
			logger.debug("Having repository: " + repository.getDirectory());
			// the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
			Ref head = repository.exactRef("refs/heads/master");
			logger.debug("Ref of refs/heads/master: " + head);

			try(Git git = new Git(repository)){
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> l = ImmutableList.copyOf(logs).reverse().iterator();
				int i = 0;
				RevCommit beforeCommit = null;
				while(l.hasNext()){
					RevCommit c = l.next();
					SystemBuilderByCommit sys = new SystemBuilderByCommit(repository,c);
					sys.readClass(null);
					GraphBuilderSystemByCommit graphB = new GraphBuilderSystemByCommit(sys.getJavas());
					graphB.createGraph(graph, beforeCommit, sys.getCommit());
					if(graphW!=null){
						graphW.write(graph, false);
						logger.debug(i+" Writed System");
					}else{
						logger.debug(i+" Saved System");
					}
					Map<String, List<String>> ixpd = new ImplicitCrossModuleDependencyDetector(graph).detect();
					if(graphW!=null){
						graphW.write(graph, false);
						logger.debug("Writed IXPD instances");
					}
//					historyIXPD.put(sys.getCommit().getId().getName(), ixpd);
					historyIXPD.put(sys.getCommit(), ixpd);
					
					RevCommit igg =sys.getCommit();
						logger.info("version commit: " + igg + " " + sys.getCommit());
						if (ixpd.isEmpty()) {
							logger.debug("***" + igg + " is not a IXPD	***");
						} else {
							
							for (String lgg:ixpd.keySet()) {
								
								if (ixpd.get(lgg).isEmpty()) {
									logger.debug("***" + igg + " is not a IXPD	***");
								} else {
//									logger.info("commit: " + lgg + " " + ixpd.get(lgg));
									printer.print(igg.getId().getName());// "version"
									printer.print(igg.getCommitTime());// "commit_time"
									printer.print(lgg);// "java-out"
									for (String e : ixpd.get(lgg)) {
										printer.print(e);
									}
									printer.println();
								}
							}
						
					}
					historyIXPD.remove(sys.getCommit());
					beforeCommit=c;
					i++;
				}
			}
			
		}
		printer.close();
		writer.close();
		printer = null;
		writer = null;
		logger.info("***CSV written***");
		return null;//return historyICPD;
	}

	//FIXME valid only for freecol repository
	@Deprecated
	public Map<String,String> writeGraphByCommitOnlyFreecolNames() throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_gitFolder);
		logger.info("repodir: "+repoDir);
		Map<String,String> m = new HashMap<String,String>();
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(repoDir)
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build()) {
			logger.debug("Having repository: " + repository.getDirectory());
			// the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
			Ref head = repository.exactRef("refs/heads/master");
			logger.debug("Ref of refs/heads/master: " + head);
			try(Git git = new Git(repository)){
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> l = ImmutableList.copyOf(logs).reverse().iterator();
				int i = 0;
				//				RevCommit beforeCommit = null;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(i+" Writed System - " +JGitUtils.getCommitDate(c).getTime()+",freecol-git"+i+"unzippati");
					m.put(JGitUtils.getCommitDate(c).getTime()+"", "freecol-git"+i+"unzippati");
					//					beforeCommit=c;
					i++;
				}
			}
		}
		return m;
	}

	//FIXME write only the history and java modifications
	@Deprecated
	public Map<String, List<String>> writeGraphByCommitOld(final Graph graph, final GraphWriter graphW) throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_gitFolder);
		logger.info("repodir: "+repoDir);
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(repoDir)
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build()) {
			logger.debug("Having repository: " + repository.getDirectory());
			// the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
			Ref head = repository.exactRef("refs/heads/master");
			logger.debug("Ref of refs/heads/master: " + head);

			try(Git git = new Git(repository)){
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> l = ImmutableList.copyOf(logs).reverse().iterator();
				int i = 0;
				RevCommit beforeCommit = null;
				while(l.hasNext()){
					RevCommit c = l.next();
					SystemBuilderByCommit sys = new SystemBuilderByCommit(repository,c);
					sys.readClass(null);
					GraphBuilderSystemByCommit graphB = new GraphBuilderSystemByCommit(sys.getJavas());
					graphB.createGraph(graph, beforeCommit, sys.getCommit());
					graphW.write(graph, false);
					logger.debug(i+" Writed System");
					beforeCommit=c;
					i++;
				}
			}
			return detect(graph);
		}
	}




	public Map<Long, Map<String,Long>> _commitModification = new HashMap<Long, Map<String,Long>>();
	//FIXME it is obsolete. ImplicitCrossModuleDepedency should be used used
	@Deprecated
	public Map<String, List<String>> detect(Graph graph) throws TypeVertexException {
		List<Edge> modification = GraphUtils.findEdgesByLabel(graph, GraphBuilderSystemByCommit.COUPLED_MODIFIED_LABEL);
		Map<String, List<String>> r = new HashMap<String, List<String>>();
		logger.info("created a hashmap and readed modification edges "+modification);
		try{
			if(modification!=null){
				logger.info("we have modifications");
				for(Edge e : modification){
					Vertex outJavaV = e.outVertex();
					Vertex inJavaV = e.inVertex();
					Iterator<Edge> packageOut = outJavaV.edges(Direction.OUT, GraphBuilderSystemByCommit.PACKAGE_DEPENDENCY_LABEL);
					Iterator<Edge> packageIn = inJavaV.edges(Direction.OUT, GraphBuilderSystemByCommit.PACKAGE_DEPENDENCY_LABEL);
					//					Iterator<Edge> commitOut = outJavaV.edges(Direction.IN, GraphBuilderSystemByCommit.MODIFIED_LABEL);
					//					Iterator<Edge> commitIn = inJavaV.edges(Direction.IN, GraphBuilderSystemByCommit.MODIFIED_LABEL);
					while(packageOut.hasNext()){
						Edge fout = packageOut.next();
						final String packageOutName = fout.inVertex().value(GraphBuilderSystemByCommit.PROPERTY_NAME);
						logger.info(String.format("package %s inserting",packageOutName));
						if(!r.containsKey(packageOutName)){
							List<String> l = new ArrayList<String>();
							r.put(packageOutName, l);
							logger.info(String.format("package %s inserted",packageOutName));
						}
						while(packageIn.hasNext()){
							Edge fin = packageIn.next();
							final String packageInName = fin.inVertex().value(GraphBuilderSystemByCommit.PROPERTY_NAME);
							final String nameOutV = outJavaV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
							final String nameInV = inJavaV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
							logger.debug(String.format("pkg-out: %s, in: %s; outJ: %s -> inJ: %s",
									packageOutName, packageInName, nameOutV,nameInV));
							List<String> l = r.get(packageOutName);
							l.add(nameInV);
							logger.info("list "+l);
							r.put(packageOutName, l);
							if(packageInName!=null&&!packageInName.equals(packageOutName)){
								if(!r.containsKey(packageInName)){
									List<String> ll = new ArrayList<String>();
									r.put(packageInName, ll);
									logger.info(String.format("package %s inserted",packageInName));
								}
								List<String> ll = r.get(packageInName);
								l.add(nameOutV);
								logger.info("list "+ll);
								r.put(packageInName, ll);
							}
						}
					}
				}

			}
			List<Edge> hasmodification = GraphUtils.findEdgesByLabel(graph, GraphBuilderSystemByCommit.MODIFIED_LABEL);
			if(modification!=null){
				logger.info("we have modifications");
				for(Edge e : hasmodification){
					Vertex outCommitV = e.outVertex();
					Vertex inJavaV = e.inVertex();
					Iterator<Edge> packageIn = inJavaV.edges(Direction.OUT, GraphBuilderSystemByCommit.PACKAGE_DEPENDENCY_LABEL);
					//					final String nameOutV = outCommitV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
					final String nameInV = inJavaV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
					final long commitDate = outCommitV.value(GraphBuilderSystemByCommit.DATE);
					logger.info(String.format("commit-out %s inserting",commitDate));
					while(packageIn.hasNext()){
						Edge fin = packageIn.next();
						final String packageInName = fin.inVertex().value(GraphBuilderSystemByCommit.PROPERTY_NAME);
						logger.info(String.format("pkgin: %s; outCommit: %s -> inJ: %s",packageInName, commitDate,nameInV));
						if(!_commitModification.containsKey(commitDate)){
							Map<String,Long> commitOutMap = new HashMap<String,Long>();
							commitOutMap.put(packageInName, 1l);
							_commitModification.put(commitDate, commitOutMap);
							logger.info(String.format("commit-out %s inserted; modified %s",commitDate,_commitModification.get(commitDate)));
						}else if(!_commitModification.get(commitDate).containsKey(packageInName)){
							_commitModification.get(commitDate).put(packageInName, 1l);
							logger.info(String.format("commit-out %s inserted; modified %s",commitDate,_commitModification.get(commitDate)));
						}else{
							long app = _commitModification.get(commitDate).get(packageInName)+1l;
							_commitModification.get(commitDate).put(packageInName, app);
							logger.info(String.format("commit-out %s inserted; modified %s",commitDate,_commitModification.get(commitDate)));
						}
					}
				}
			}
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return r;
	}


	private File openSampleGitRepo(final File systemPath) throws IOException, GitAPIException {
		try (Repository repository = openRepository(systemPath)) {
			logger.info("Repository at " + repository.getDirectory());
			File dir = repository.getDirectory();
			return dir;
		}
	}

	private Repository openRepository(final File systemPath) throws IOException{
		// open the directory
		Repository repository = new FileRepository(systemPath);
		return repository;
	}
}
