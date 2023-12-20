//==============================================================================
package com.edrosframework.placartenis1.events;

import java.util.EventObject;

//------------------------------------------------------------------------------
// this class should be "immutable"
public class ToggleServiceEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private final int current_server_id;
    
    //--------------------------------------------
    public ToggleServiceEvent(Object source, int newServer ) {
        super( source );
        current_server_id = newServer;
    }

    //--------------------------------------------
    public int Position() {
        return current_server_id;
    }
}
//==============================================================================