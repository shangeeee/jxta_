package service.chat.bind;

import service.chat.bind.message.BindMessageResponse;
import java.util.EventObject;

public class BindQueryEvent extends EventObject {

    private final BindMessageResponse bindResponse;

    public BindQueryEvent(Object source, BindMessageResponse bindResponse) {
        super(source);
        this.bindResponse = bindResponse;
    }

    public BindMessageResponse getbindResponse() {
        return bindResponse;
    }

}
