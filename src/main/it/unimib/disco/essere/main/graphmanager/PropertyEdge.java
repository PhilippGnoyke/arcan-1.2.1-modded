package it.unimib.disco.essere.main.graphmanager;

public enum PropertyEdge {
	LABEL_PACKAGE_DEPENDENCY("belongsTo"),
	LABEL_CLASS_DEPENDENCY("dependsOn"),
	LABEL_SUPER_DEPENDENCY("isChildOf"),
	LABEL_INTERFACE_DEPENDENCY("isImplementationOf"),
	LABEL_EFFERENCE("isEfferentOf"),
	LABEL_BACKREF("backref"); //Modded
	//edge labels
	private final String _property;
	/**
     * @param text
     */
    private PropertyEdge(final String property) {
    	_property = property;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return _property;
    }

}
