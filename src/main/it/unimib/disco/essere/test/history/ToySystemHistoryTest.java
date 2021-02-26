package it.unimib.disco.essere.test.history;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.archive.ArchiveFormats;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.main.asengine.ImplicitCrossModuleDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphBuilderSystemByCommit;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByCommit;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

@RunWith(JUnit4.class)
public class ToySystemHistoryTest {
	private static final Logger logger = LogManager.getLogger(ToySystemHistoryTest.class);

	static Path _g = Paths.get("C:", "Users", "ricca", "Documents", "Neo4j-commit-test", "default.graphdb");
	//	Path s = Paths.get("C:", "Users", "ricca", "workspaceThinkerpop", "ToySystem", "target", "classes", "it", "unimib", "disco", "essere", "toysystem");
	Path _s = Paths.get("target", "classes", "it", "unimib", "disco", "essere", "toysystem").toAbsolutePath();
	//	Path r = Paths.get("C:", "Users", "ricca", "workspaceThinkerpop", "ToySystem",".git").toAbsolutePath();
	Path _r = Paths.get(".git").toAbsolutePath();
	Path _rtoytest = Paths.get("C:","gittest","ToySystem",".git").toAbsolutePath();
	Path _rquartz = Paths.get("C:","gittest","quartz",".git").toAbsolutePath();
	Path _racommons = Paths.get("C:","gittest","commons-collections",".git").toAbsolutePath();

	static Graph _graph = null;
	static GraphWriter _graphW = null;

	/**
	 * Setup of the neo4j graph, it is only opened and initialized
	 */
	@BeforeClass
	public static void setupGraphopen() {
		_graphW = new Neo4JGraphWriter();
		_graphW.setup(_g.toString());
		_graph = _graphW.init();
		logger.debug("Initialized Neo4j");
	}

	/**
	 * test to write a system to the neo4j graph
	 */
	@Test
	public void writeToysystem(){
		SystemBuilder sys = new SystemBuilderByUrl();
		sys.readClass(_s.toString());
		GraphBuilder graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());
		graphB.createGraph(_graph);
		_graphW.write(_graph, false);
		logger.debug("Writed ToySystem");

	}

	@Test
	public void extractJavaNameClassAndPackage(){
		String[] classInfo = new String[2];
		String fullClassName = "src/it/unimib/disco/essere/analysis/file/util/DirUtils.java";

		int slashIndex = fullClassName.lastIndexOf('/');
		String packageName = fullClassName.substring(0, slashIndex);
		packageName = packageName.replace('/', '.');

		String className = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
		className = className.replace('/', '.');

		classInfo[0] = className;
		classInfo[1] = packageName;

		logger.debug("classname: "+className);
		logger.debug("packageName: "+packageName);

		assertEquals(className, "src.it.unimib.disco.essere.analysis.file.util.DirUtils");
		assertEquals(packageName, "src.it.unimib.disco.essere.analysis.file.util");
	}

	/**
	 * test to write a system by commit to the neo4j graph
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws TypeVertexException 
	 */
	@Test
	public void writeToysystemByCommit() throws IOException, GitAPIException, TypeVertexException{
		logger.info("Start: ");
		writeGraphByCommit(_r.toFile());
	}
	
	/**
	 * test to write a system by commit to the neo4j graph
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws TypeVertexException 
	 */
	@Test
	public void writeQuartzByCommit() throws IOException, GitAPIException, TypeVertexException{
		logger.info("Start: ");
		writeGraphByCommit(_rquartz.toFile());
	}
	
	/**
	 * test to write a system by commit to the neo4j graph
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws TypeVertexException 
	 */
	@Test
	public void writeApacheCommonsByCommit() throws IOException, GitAPIException, TypeVertexException{
		logger.info("Start: ");
		writeGraphByCommit(_racommons.toFile());
	}

	public static void writeGraphByCommit(File file) throws IOException, GitAPIException, NoHeadException, TypeVertexException {
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(file);
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
					graphB.createGraph(_graph, beforeCommit, sys.getCommit());
					_graphW.write(_graph, false);
					logger.debug(i+" Writed System");
					beforeCommit=c;
					i++;
				}
			}
			new ImplicitCrossModuleDependencyDetector(_graph).detect();
			_graphW.write(_graph, false);
			logger.debug(" Writed IXPD instances");
		}
	}
	
	/**
	 * Read the hash code of all commits
	 * 
	 * The commits are ordered by descended date. Commit at index 0 is the most
	 * recent one and there is the first commit at last index
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Test
	public void readCommitSympleProjectTextComment() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = createSampleGitRepo();
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
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					assertEquals("Added testfile", c.getShortMessage());
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					assertEquals("Added testfile", c.getFullMessage());
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					assertEquals(0, c.getParentCount());
					i++;
				}
				assertEquals(1, i);
			}
		}
		logger.info("Exit ");
	}

	/**
	 * Read and print all the information of the commit, e.g., comment, author
	 * and hashcode. Also print for all modified files the insertion and deletions.
	 * 
	 * The commits are ordered by descended date. Commit at index 0 is the most
	 * recent one and there is the first commit at last index
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Test
	public void readCommitToySystemFilesModified() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_r.toFile());
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
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					for(PathChangeModel f :JGitUtils.getFilesInCommit(repository, c)){
						logger.info(String.format("%s - name: %s %s %s", i,f.insertions,f.deletions,f.name));
					}
					//					DiffUtils.getDiffStat(repository, c);
					i++;
				}
			}
		}
		logger.info("Exit ");
	}

	/**
	 * Read and print all the information of the commit, e.g., comment, author
	 * and hashcode
	 * 
	 * The commits are ordered by descended date. Commit at index 0 is the most
	 * recent one and there is the first commit at last index
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Test
	public void readCommitToySystemTextComment() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_r.toFile());
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
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					i++;
				}
			}
		}
		logger.info("Exit ");
	}


	/**
	 * Checkout on the first commit for the simple repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Test
	public void checkoutSympleSystem() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = createSampleGitRepo();
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

			try (Git git = new Git(repository)) {
				git.add()
				.addFilepattern("testfile-log")
				.call();
				// and then commit the changes
				git.commit()
				.setMessage("Added testfile-log")
				.call();
			}

			try(Git git = new Git(repository)){
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(i+" - "+ c.getId().name());
					//bisogna mettere il nome del branch
					CheckoutCommand r =git.checkout().setName(head.getName()).setStartPoint(c);
					r.call();
					logger.info(r.getResult().getStatus());
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					i++;
				}
				assertEquals(2, i);
			}
		}
		logger.info("Exit ");
	}

	@Test
	public void archiveZipToySystemSystem() throws IOException, GitAPIException{
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_rtoytest.toFile());
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
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				String zip = "zip";
				//				git.archive().registerFormat(zip, new ZipFormat());
				ArchiveFormats.registerAll();
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(i+" - "+ c.getId().name());
					//bisogna mettere il nome del branch
					Path rtoytest2 = Paths.get("C:","gittest","ToySystem"+i,".git").toAbsolutePath();
					//					CheckoutCommand r =git.checkout().setName(c.getId().name()).setStartPoint(c.getId().name()).addPath(rtoytest2.toString());
					Paths.get("C:","gittest","ToySystem"+i+".zippato").toFile().toPath().toString();
					//					r.call();
					//					logger.info(r.getStatus());
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - ObjectId:     %s", i,c.toObjectId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					ArchiveCommand r = git.archive().setFormat(zip).setTree(repository.resolve(c.getId().getName()));//.setFilename("zippato.zip")
					logger.info(String.format("%s - is null?      %b", i, r==null));
					FileOutputStream fos = new FileOutputStream(Paths.get("C:","gittest","ToySystem"+i+"zippato.zip").toFile());
					//					ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
					r.setOutputStream(fos);
					r.call();
					//					zos.close();
					fos.close();
					i++;
				}
				assertEquals(66, i);
			}
		}
		logger.info("Exit ");

	}

	@Test
	public void createBranchForCommitToySystemSystem() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_rtoytest.toFile());
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
				Iterator<RevCommit> l = logs.iterator();
				int i = 0;
				while(l.hasNext()){
					RevCommit c = l.next();
					logger.info(i+" - "+ c.getId().name());
					//bisogna mettere il nome del branch
					CreateBranchCommand r =git.branchCreate().setName(c.getId().name()).setStartPoint(c);
					r.call();
					//					logger.info(r.getStatus());
					logger.info(String.format("%s - %s", i,c));
					logger.info(String.format("%s - id:           %s", i,c.getId()));
					logger.info(String.format("%s - shortMessage: %s", i,c.getShortMessage()));
					logger.info(String.format("%s - fullMessage:  %s", i,c.getFullMessage()));
					logger.info(String.format("%s - name:         %s", i,c.getName()));
					logger.info(String.format("%s - authorident:  %s", i,c.getAuthorIdent()));
					logger.info(String.format("%s - parent count: %s", i,c.getParentCount()));
					i++;
				}
				assertEquals(72, i);
			}
		}
		logger.info("Exit ");
	}

	/**
	 * Print info of all branches
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Test
	public void printBranchToySystem() throws IOException, GitAPIException {
		logger.info("Start: ");
		// first create a test-repository, the return is including the .git directory here!
		File repoDir = openSampleGitRepo(_r.toFile());
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

			int i=0;
			Map<String, Ref> l=repository.getAllRefs();
			for(String s:l.keySet()){
				logger.info(String.format("%s - %s", i,l.get(s)));
				logger.info(String.format("%s - name:       %s", i,l.get(s).getName()));
				logger.info(String.format("%s - objid:		%s", i,l.get(s).getObjectId()));
				logger.info(String.format("%s - target:  	%s", i,l.get(s).getTarget()));
				logger.info(String.format("%s - leaf:       %s", i,l.get(s).getLeaf()));
				logger.info(String.format("%s - peeledobjid:%s", i,l.get(s).getPeeledObjectId()));
				logger.info(String.format("%s - storage: 	%s", i,l.get(s).getStorage()));
				i++;
			}
			logger.info("Exit ");
		}
	}


	/**
	 * test for the generalization of the path for other test made automatically
	 */
	@Test
	public void pathProjectTest(){
		Path s = Paths.get("C:", "Users", "ricca", "workspaceThinkerpop", "ToySystem");
		Path p = new File("").toPath().toAbsolutePath();
		assertEquals(p,s);
		logger.debug(String.format("path %s = %s",p.toString(),s.toString()));

		s = Paths.get("C:", "Users", "ricca", "workspaceThinkerpop", "ToySystem", "target", "classes", "it", "unimib", "disco", "essere", "toysystem");
		p = Paths.get("target", "classes", "it", "unimib", "disco", "essere", "toysystem").toAbsolutePath();
		assertEquals(p,s);
		logger.debug(String.format("path %s = %s",p.toString(),s.toString()));
	}

	@AfterClass
	public static void closeGraph() {
		try {
			if (_graph != null) {
				_graph.close();
				logger.debug("Closed stream graph");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Repository createNewRepository() throws IOException{
		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();
		// create the directory
		Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
		repository.create();
		return repository;
	}

	private static Repository openRepository(final File systemPath) throws IOException{
		// open the directory
		Repository repository = new FileRepository(systemPath);
		return repository;
	}

	private static File openSampleGitRepo(final File systemPath) throws IOException, GitAPIException {
		try (Repository repository = openRepository(systemPath)) {
			logger.info("Repository at " + repository.getDirectory());
			File dir = repository.getDirectory();
			return dir;
		}
	}

	private static File createSampleGitRepo() throws IOException, GitAPIException {
		try (Repository repository = createNewRepository()) {
			logger.info("Temporary repository at " + repository.getDirectory());
			// create the file
			File myfile = new File(repository.getDirectory().getParent(), "testfile");
			myfile.createNewFile();
			// run the add-call
			try (Git git = new Git(repository)) {
				git.add()
				.addFilepattern("testfile")
				.call();
				// and then commit the changes
				git.commit()
				.setMessage("Added testfile")
				.call();
			}
			logger.info("Added file " + myfile + " to repository at " + repository.getDirectory());
			File dir = repository.getDirectory();
			return dir;
		}
	}
}
