package org.correomqtt.business.provider;

import org.correomqtt.business.dispatcher.ConfigDispatcher;
import org.correomqtt.business.dispatcher.ConfigObserver;
import org.correomqtt.business.dispatcher.ConnectionLifecycleDispatcher;
import org.correomqtt.business.dispatcher.ConnectionLifecycleObserver;
import org.correomqtt.business.dispatcher.PersistSubscriptionHistoryDispatcher;
import org.correomqtt.business.dispatcher.SubscribeGlobalDispatcher;
import org.correomqtt.business.dispatcher.SubscribeGlobalObserver;
import org.correomqtt.business.model.SubscriptionDTO;
import org.correomqtt.business.model.SubscriptionHistoryListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistSubscriptionHistoryProvider extends BasePersistHistoryProvider<SubscriptionHistoryListDTO>
        implements SubscribeGlobalObserver,
        ConnectionLifecycleObserver,
        ConfigObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistSubscriptionHistoryProvider.class);

    private static final String HISTORY_FILE_NAME = "subscriptionHistory.json";

    private static final int MAX_ENTRIES = 100;

    private static Map<String, PersistSubscriptionHistoryProvider> instances = new HashMap<>();
    private static Map<String, SubscriptionHistoryListDTO> historyDTOs = new HashMap<>();

    private PersistSubscriptionHistoryProvider(String id) {
        super(id);
        SubscribeGlobalDispatcher.getInstance().addObserver(this);
        ConnectionLifecycleDispatcher.getInstance().addObserver(this);
        ConfigDispatcher.getInstance().addObserver(this);
    }

    @Override
    protected void readingError(Exception e) {
        PersistSubscriptionHistoryDispatcher.getInstance().errorReadingSubscriptionHistory(e);
    }

    public static void activate(String id) {
        instances.computeIfAbsent(id, PersistSubscriptionHistoryProvider::new);
    }

    public static synchronized PersistSubscriptionHistoryProvider getInstance(String id) {
        return instances.computeIfAbsent(id, PersistSubscriptionHistoryProvider::new);
    }

    @Override
    String getHistoryFileName() {
        return HISTORY_FILE_NAME;
    }

    @Override
    Class<SubscriptionHistoryListDTO> getDTOClass() {
        return SubscriptionHistoryListDTO.class;
    }

    @Override
    void setDTO(String id, SubscriptionHistoryListDTO dto) {
        historyDTOs.put(id, dto);
    }

    public List<String> getTopics(String connectionId) {
        if(historyDTOs.get(connectionId) == null){
            setDTO(connectionId, new SubscriptionHistoryListDTO(new ArrayList<>()));
        }
        return historyDTOs.get(connectionId).getTopics();
    }

    @Override
    public void onSubscribedSucceeded(String connectionId, SubscriptionDTO subscriptionDTO) {

        if (subscriptionDTO.isHidden()) {
            return;
        }

        LOGGER.info("Persisting new subscription history entry: {}", subscriptionDTO.getTopic());

        List<String> topicsSet = getTopics(connectionId);
        String topic = subscriptionDTO.getTopic();
        topicsSet.remove(topic);
        topicsSet.add(topic);
        while (topicsSet.size() > MAX_ENTRIES) {
            LOGGER.info("Removing last entry from subscription history, cause limit of {} is reached.", MAX_ENTRIES);
            topicsSet.remove(topicsSet.iterator().next());
        }

        saveHistory(connectionId);
    }

    private void saveHistory(String connectionId) {
        try {
            new ObjectMapper().writeValue(getFile(), historyDTOs.get(connectionId));
            PersistSubscriptionHistoryDispatcher.getInstance().updatedSubscriptions(connectionId);
        } catch (IOException e) {
            LOGGER.error("Failed to write " + getHistoryFileName(), e);
            PersistSubscriptionHistoryDispatcher.getInstance().errorWritingSubscriptionHistory(e);
        }
    }

    @Override
    public void onSubscribeRemoved(String connectionId, SubscriptionDTO subscriptionDTO) {
        // nothing to do
    }

    @Override
    public void onSubscribeCleared(String connectionId) {
        // nothing to do
    }

    @Override
    public void onConfigDirectoryEmpty() {
        // nothing to do
    }

    @Override
    public void onConfigDirectoryNotAccessible() {
        // nothing to do
    }

    @Override
    public void onAppDataNull() {
        // nothing to do
    }

    @Override
    public void onUserHomeNull() {
        // nothing to do
    }

    @Override
    public void onFileAlreadyExists() {
        // nothing to do
    }

    @Override
    public void onInvalidPath() {
        // nothing to do
    }

    @Override
    public void onInvalidJsonFormat() {
        // nothing to do
    }

    @Override
    public void onSavingFailed() {
        // nothing to do
    }

    @Override
    public void onSettingsUpdated() {
        // nothing to do
    }

    @Override
    public void onConnectionsUpdated() {
        removeFileIfConnectionDeleted();
    }

    @Override
    public void onConfigPrepareFailed() {
        // nothing to do
    }

    @Override
    public void onDisconnectFromConnectionDeleted(String connectionId) {
        // nothing to do
    }

    @Override
    public void onConnect() {
        // nothing to do
    }

    @Override
    public void onConnectRunning() {
        // nothing to do
    }

    @Override
    public void onConnectionFailed(Throwable message) {
        // nothing to do
    }

    @Override
    public void onConnectionLost() {
        // nothing to do
    }

    @Override
    public void onDisconnect() {
        instances.remove(getConnectionId());
        historyDTOs.remove(getConnectionId());
    }

    @Override
    public void onDisconnectFailed(Throwable exception) {
        // nothing to do
    }

    @Override
    public void onDisconnectRunning() {
        // nothing to do
    }

    @Override
    public void onConnectionReconnected() {
        // nothing to do
    }

    @Override
    public void onReconnectFailed(AtomicInteger triedReconnects, int maxReconnects) {
        // nothing to do
    }
}


