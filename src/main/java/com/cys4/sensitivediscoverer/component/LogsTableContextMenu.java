/*
Copyright (C) 2023 CYS4 Srl
See the file 'LICENSE' for copying permission
*/
package com.cys4.sensitivediscoverer.component;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.cys4.sensitivediscoverer.model.LogEntity;
import com.cys4.sensitivediscoverer.model.LogsTableModel;
import com.cys4.sensitivediscoverer.model.RegexEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.cys4.sensitivediscoverer.Messages.getLocaleString;

public class LogsTableContextMenu extends JPopupMenu {

    public LogsTableContextMenu(LogEntity logEntry,
                                List<LogEntity> logEntries,
                                HttpRequestEditor originalRequestViewer,
                                HttpResponseEditor originalResponseViewer,
                                LogsTableModel logsTableModel,
                                LogsTable logsTable,
                                MontoyaApi burpApi,
                                boolean isAnalysisRunning) {
        RegexEntity regexEntity = logEntry.getRegexEntity();
        ProxyHttpRequestResponse requestResponse = logEntry.getRequestResponse();
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        JMenuItem sendToRepeater = new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-sendToRepeater")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                burpApi.repeater().sendToRepeater(requestResponse.finalRequest(), regexEntity.getDescription());
            }
        });
        this.add(sendToRepeater);

        JMenuItem sendToIntruder = new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-sendToIntruder")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                burpApi.intruder().sendToIntruder(requestResponse.finalRequest());
            }
        });
        this.add(sendToIntruder);

        JMenuItem sendToOrganizer = new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-sendToOrganizer")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                burpApi.organizer().sendToOrganizer(HttpRequestResponse.httpRequestResponse(
                        requestResponse.finalRequest(),
                        requestResponse.response(),
                        Annotations.annotations(logEntry.getMatch())));
            }
        });
        this.add(sendToOrganizer);

        JMenu sendToComparer = new JMenu(getLocaleString("logger-ctxMenu-sendToComparer"));
        JMenuItem comparerRequest = new JMenuItem(new AbstractAction(getLocaleString("common-request")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                burpApi.comparer().sendToComparer(requestResponse.finalRequest().toByteArray());
            }
        });
        JMenuItem comparerResponse = new JMenuItem(new AbstractAction(getLocaleString("common-response")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                burpApi.comparer().sendToComparer(requestResponse.response().toByteArray());
            }
        });
        sendToComparer.add(comparerRequest);
        sendToComparer.add(comparerResponse);
        this.add(sendToComparer);

        JMenuItem sendToDecoder = new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-sendToDecoder")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                burpApi.decoder().sendToDecoder(ByteArray.byteArray(logEntry.getMatch()));
            }
        });
        this.add(sendToDecoder);

        this.add(new JPopupMenu.Separator());
        JMenuItem removeItem = new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-removeItem")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                logEntries.remove(logEntry);

                int rowIndex = logsTable.getSelectedRow();
                if (rowIndex == -1) return;
                int realRow = logsTable.convertRowIndexToModel(rowIndex);
                logsTableModel.fireTableRowsDeleted(realRow, realRow);

                originalResponseViewer.setResponse(HttpResponse.httpResponse(""));
                originalRequestViewer.setRequest(HttpRequest.httpRequest(""));
            }
        });
        if (isAnalysisRunning) removeItem.setEnabled(false);
        this.add(removeItem);

        this.add(new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-copyURL")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                StringSelection selection = new StringSelection(requestResponse.finalRequest().url());
                systemClipboard.setContents(selection, selection);
            }
        }));

        this.add(new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-copyDescription")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                StringSelection selection = new StringSelection(regexEntity.getDescription());
                systemClipboard.setContents(selection, selection);
            }
        }));

        this.add(new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-copyRegex")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                StringSelection selection = new StringSelection(regexEntity.getRegex());
                systemClipboard.setContents(selection, selection);
            }
        }));

        this.add(new JMenuItem(new AbstractAction(getLocaleString("logger-ctxMenu-copyMatch")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                StringSelection selection = new StringSelection(logEntry.getMatch());
                systemClipboard.setContents(selection, selection);
            }
        }));
    }
}
