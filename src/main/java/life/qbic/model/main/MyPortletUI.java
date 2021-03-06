package life.qbic.model.main;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import life.qbic.presenter.MainPresenter;
import life.qbic.view.MainView;

@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.AppWidgetSet")
public class MyPortletUI extends UI {

    private static final Log log = LogFactoryUtil.getLog(MyPortletUI.class.getName());
    public static final String toolname = "Printer-Garden v. -1";

    @Override
    protected void init(VaadinRequest request) {

        MainView mainView = new MainView(this);
        MainPresenter mainPresenter = new MainPresenter(mainView, this);

    }
}
