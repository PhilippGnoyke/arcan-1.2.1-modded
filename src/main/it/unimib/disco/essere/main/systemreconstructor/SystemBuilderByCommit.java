package it.unimib.disco.essere.main.systemreconstructor;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class SystemBuilderByCommit extends SystemBuilder {
	private static final Logger logger = LogManager.getLogger(SystemBuilderByCommit.class);
	RevCommit _commit = null;
	Repository _repository = null;
	Set<PathChangeModel> _javas = null;
	
	public SystemBuilderByCommit(final Repository repository, final RevCommit c) {
		_commit = c;
		_repository = repository;
	}
	
	@Override
	public void readClass(String url) {
		_javas = new HashSet<PathChangeModel>();
		for(PathChangeModel f :JGitUtils.getFilesInCommit(_repository, _commit)){
			logger.debug(String.format("name: %s %s %s",f.insertions,f.deletions,f.name));
			if(f!=null&&f.isFile()&&f.name.endsWith(".java")){
				_javas.add(f);
			}
		}
	}
	
	public Set<PathChangeModel> getJavas(){
		return _javas;
	}

	public RevCommit getCommit() {
		return _commit;
	}

}
