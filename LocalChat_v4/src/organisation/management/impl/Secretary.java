/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.impl;

import java.io.IOException;
import java.util.logging.Logger;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import organisation.management.Administration;
import organisation.management.Member;
import organisation.management.protocol.calendar.CalendarAction;

/**
 *
 * @author The Boss
 */
public class Secretary extends Administration {
private static final Logger Log = Logger.getLogger(Secretary.class.getName());

    public static final String role = "Secretary";
    OutputPipe calendarMessageOutputPipe;

    public Secretary() {

    }

    @Override
    public boolean startWorking() {
        if (super.startWorking()) {
            return true;
        }
        return false;
    }

    @Override
    public void stopWorking() {
        Log.info("Stopping Responsable role ---");
        super.stopWorking();
        calendarMessageOutputPipe.close();
        calendarMessageOutputPipe=null;
        System.gc();
    }

    /**
     * create an output pipe to notify all calendar changes
     *
     * @throws java.io.IOException
     */
    protected void buildCalendarOutMessagePipe() throws IOException {
        //build the output pipe
        if (calendarMessageOutputPipe == null || calendarMessageOutputPipe.isClosed()) {
            PeerGroup group = controller.getGroup();
            PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.
                    newAdvertisement(PipeAdvertisement.getAdvertisementType());
            //Build a well know pipeID using the peer id and the class name
            PipeID pipeID = IDFactory.newPipeID(group.getPeerGroupID(), Member.class.getName().getBytes());
            pipeAdv.setPipeID(pipeID);
            pipeAdv.setType(PipeService.PropagateType);
            pipeAdv.setName("CalendarMessage");
            pipeAdv.setDescription("PropagatePipe for the sercretary to share calendar information");

            group.getPipeService().createOutputPipe(pipeAdv, (OutputPipeEvent event)
                    -> {
                System.out.println("Output pipe created ///");
                calendarMessageOutputPipe = event.getOutputPipe();
                //System.out.p rintln(calendarMessageOutputPipe.getPipeID());
            });
        }
    }

    public void sendNewCalendarAction(CalendarAction action) throws IOException {
        buildCalendarOutMessagePipe();

        while (calendarMessageOutputPipe == null) {
            System.out.println("Not bind yet ");
        }
        Message msg = new Message();
        StringMessageElement msgElement = new StringMessageElement(action.getType(), action.toString(), null);
        msg.addMessageElement(msgElement);

        calendarMessageOutputPipe.send(msg);

    }

    @Override
    public String getRole() {
        return role;
    }

}
