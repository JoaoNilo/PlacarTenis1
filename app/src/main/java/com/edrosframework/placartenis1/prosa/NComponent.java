//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_NULL;

//--------------------------------------------------------------------------------------------------
public class NComponent {
    int Tag;
    int Handle;

    //----------------------------------------------------------------------------------------------
    // Methods
    public NComponent(){
        Tag = 0; Handle = this.hashCode();
    }

    public void Notify(NMESSAGE msg){
        msg.message = NM_NULL;
    }

    public void InterruptCallBack(NMESSAGE msg){
        msg.message = NM_NULL;
    }
}
//==================================================================================================