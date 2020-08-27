package service.chat.bind;

import java.util.EventListener;

public interface BindQueryListener extends EventListener{

	public void bindQueryAccepted(BindQueryEvent bindEvent);
}
