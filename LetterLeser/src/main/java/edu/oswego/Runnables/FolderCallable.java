package edu.oswego.Runnables;

import edu.oswego.model.Email;

import java.util.List;
import java.util.concurrent.Callable;

public class FolderCallable implements Callable {
	private List<Email> folders;

	public FolderCallable(List<Email> folders) {
		this.folders = folders;
	}

	@Override
	public Object call() throws Exception {
		String answer = "";
		for (int i = 0; i < folders.size(); i++) {

		}

		return answer;
	}
}
