package com.hazelcast.samples.hazelcastsavedmydata;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hazelcast.samples.hazelcastsavedmydata.Styles.BACKGROUND_DARK_BLUE;
import static com.hazelcast.samples.hazelcastsavedmydata.Styles.COLOR_WHITE;
import static com.hazelcast.samples.hazelcastsavedmydata.Styles.FONT_20PX;
import static com.hazelcast.samples.hazelcastsavedmydata.Styles.FONT_BOLD;
import static com.hazelcast.samples.hazelcastsavedmydata.Styles.DATA_BORDER;
import static com.hazelcast.samples.hazelcastsavedmydata.Styles.TITLE_PADDING_BOTTOM;

/**
 * This UI is the application entry point. A UI may either represent a browser window (or tab) or some part of a html page where a
 * Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be overridden to add component to the user
 * interface and initialize non-component functionality.
 */
@Push(PushMode.MANUAL)
@Theme("mytheme")
public class MyUI
        extends UI {

    private final HorizontalLayout mainLayout = new HorizontalLayout();
    private final VerticalLayout entryWrapLayout = new VerticalLayout();
    private final VerticalLayout membersWrapLayout = new VerticalLayout();
    private final VerticalLayout entryLayout = new VerticalLayout();
    private final VerticalLayout membersLayout = new VerticalLayout();
    private CssLayout dataLayout;
    private ConcurrentHashMap<String, String> keys;
    private Map<String, VerticalLayout> dataMap = new HashMap<>();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        initUI();
    }

    private void initUI() {
        mainLayout.setSizeFull();

        initWrapperLayouts();

        initEntryLayout();
        initMembersLayout();

        entryWrapLayout.addComponent(entryLayout);
        membersWrapLayout.addComponent(membersLayout);

        mainLayout.addComponents(entryWrapLayout, membersWrapLayout);
        mainLayout.setExpandRatio(entryWrapLayout, (float) 0.36);
        mainLayout.setExpandRatio(membersWrapLayout, (float) 0.64);

        setContent(mainLayout);
    }

    private void initWrapperLayouts() {
        entryWrapLayout.addStyleName(BACKGROUND_DARK_BLUE);
        entryWrapLayout.setSizeFull();
        entryWrapLayout.setSpacing(false);
        entryWrapLayout.setMargin(false);
        membersWrapLayout.addStyleName(BACKGROUND_DARK_BLUE);
        membersWrapLayout.setSizeFull();
        membersWrapLayout.setSpacing(false);
        membersWrapLayout.setMargin(false);
    }
    private void initEntryLayout() {
        entryLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        String basepath = VaadinService.getCurrent()
                                       .getBaseDirectory().getAbsolutePath();
        FileResource resource = new FileResource(new File(basepath +
                "/WEB-INF/images/hazelcast-logo.png"));
        Image logo = new Image("", resource);
        entryLayout.addComponent(logo);

        Label titleLabel = new Label("Hazelcast saved my data!");
        titleLabel.addStyleName(FONT_BOLD);
        titleLabel.addStyleName(FONT_20PX);
        titleLabel.addStyleName(COLOR_WHITE);
        titleLabel.addStyleName(TITLE_PADDING_BOTTOM);
        entryLayout.addComponent(titleLabel);

        Label seperator = new Label();
        seperator.setContentMode(ContentMode.HTML);
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            html.append(VaadinIcons.LINE_H.getHtml());
        }
        seperator.setValue(html.toString());
        entryLayout.addComponent(seperator);

        Button insertingData = new Button("Start inserting data");
        insertingData.addStyleName("button-icon");
        insertingData.setIcon(VaadinIcons.MAGIC);
        insertingData.addClickListener(e -> insertData());
        insertingData.setWidth(100, Unit.PERCENTAGE);
        entryLayout.addComponent(insertingData);

        Label seperator2 = new Label();
        seperator2.setContentMode(ContentMode.HTML);
        seperator2.setValue(html.toString());
        entryLayout.addComponent(seperator2);

        HorizontalLayout validateFormLayout = new HorizontalLayout();
        validateFormLayout.setWidth(100, Unit.PERCENTAGE);
        Button validateButton = new Button("Validate!");
        validateButton.addStyleName("button-icon");
        validateButton.setIcon(VaadinIcons.MAGIC);
        validateButton.addClickListener(e -> validate());
        validateButton.setWidth(100, Unit.PERCENTAGE);
        validateFormLayout.addComponents(validateButton);
        entryLayout.addComponent(validateFormLayout);

        Label connectedLabel = new Label("Connected!");
        connectedLabel.addStyleName(FONT_BOLD);
        connectedLabel.addStyleName(FONT_20PX);
        connectedLabel.addStyleName(COLOR_WHITE);
        connectedLabel.addStyleName(TITLE_PADDING_BOTTOM);
        connectedLabel.setVisible(false);
        entryLayout.addComponent(connectedLabel);

    }

    private void insertData() {
        int threads = 41;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        keys = new ConcurrentHashMap<>();

        executorService.submit((Runnable) () -> {
            while (true) {
                System.out.println("Entries: " + keys.size());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        long x = 0;
        for (int i = 1; i < threads; i++) {
            executorService.submit(new HazelcastTask(keys, x));
            x += 10000000;
        }

    }

    public static class HazelcastTask
            implements Callable<Boolean> {
        private ConcurrentHashMap<String, String> keys;
        private long i;

        public HazelcastTask(ConcurrentHashMap<String, String> keys, long i) {
            this.keys = keys;
            this.i = i;
        }

        public Boolean call() {
            HazelcastInstance client = HazelcastClient.newHazelcastClient();
            IMap<Object, Object> myMap = client.getMap("myMap");
            long threshold = i + 8;

            while (true) {
                if (i > threshold) {
                    return false;
                }
                String key = i++ + "";
                try {
                    myMap.set(key, new String(new byte[10000000]));
                    keys.put(key, "v");
                } catch (Exception e) {
                    System.out.println(key);
                    return true;
                }
            }
        }
    }


    private void validate() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<Object, Object> myMap = client.getMap("myMap");


        for (String key : keys.keySet()) {
            Object val = myMap.get(key);
            String style = "background-greenA200";
            if (val == null){
                style = "background-red";
            }
            dataMap.get(key).setStyleName(style);
            push();
        }
    }

    private void initMembersLayout() {
        membersLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

        Label myDataLabel = new Label("My Data");
        myDataLabel.addStyleName(COLOR_WHITE);
        myDataLabel.addStyleName(FONT_20PX);
        membersLayout.addComponent(myDataLabel);

        Button showMyDataButton = new Button("Show my Data");
        showMyDataButton.addClickListener(listener -> showMyData());
        membersLayout.addComponent(showMyDataButton);

        dataLayout = new CssLayout();
        dataLayout.setWidth(100, Unit.PERCENTAGE);
        dataLayout.addStyleName("member-internal-spacing");
        dataLayout.addStyleName(DATA_BORDER);

        membersLayout.addComponent(dataLayout);
    }

    private void showMyData() {
        for (String key : keys.keySet()) {
            VerticalLayout data = new VerticalLayout();
            data.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            data.setWidth(32, Unit.PIXELS);
            data.setHeight(32, Unit.PIXELS);
            data.setMargin(false);
            data.addStyleName("member-padding");
            data.setStyleName(DATA_BORDER);

            Label dataLabel = new Label("+");
            dataLabel.addStyleName(FONT_BOLD);
            dataLabel.addStyleName(FONT_20PX);
            dataLabel.addStyleName(COLOR_WHITE);
            dataLabel.addStyleName(TITLE_PADDING_BOTTOM);
            data.addComponent(dataLabel);
            dataLayout.addComponent(data);

            dataMap.put(key, data);
            push();
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet
            extends VaadinServlet {
    }
}
