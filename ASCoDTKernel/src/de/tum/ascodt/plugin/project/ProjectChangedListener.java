package de.tum.ascodt.plugin.project;

public interface ProjectChangedListener {
	void begin();
	void end();
	public void notify(Project project);
	//public void update(Project project);
}
