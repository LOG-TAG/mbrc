package com.kelsos.mbrc.commands;

import com.google.inject.Inject;
import com.kelsos.mbrc.model.SocketMessage;
import com.kelsos.mbrc.interfaces.ICommand;
import com.kelsos.mbrc.interfaces.IEvent;
import com.kelsos.mbrc.util.NotificationService;
import com.kelsos.mbrc.model.MainDataModel;
import com.kelsos.mbrc.constants.Protocol;
import com.kelsos.mbrc.net.SocketService;

public class ConnectionStatusChangedCommand implements ICommand {
    private MainDataModel model;
    private SocketService service;
    private NotificationService notificationService;

    @Inject public ConnectionStatusChangedCommand(MainDataModel model, SocketService service, NotificationService notificationService) {
        this.model = model;
        this.service = service;
        this.notificationService = notificationService;
    }

    public void execute(IEvent e) {
        model.setConnectionState(e.getDataString());
        if (model.getIsConnectionActive()) {
            service.sendData(new SocketMessage(Protocol.Player, Protocol.Request, "Android"));
        } else {
            notificationService.cancelNotification(NotificationService.NOW_PLAYING_PLACEHOLDER);
        }
    }
}
