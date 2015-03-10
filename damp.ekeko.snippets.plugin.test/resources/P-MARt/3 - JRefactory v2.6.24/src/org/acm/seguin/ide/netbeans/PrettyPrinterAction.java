package org.acm.seguin.ide.netbeans;

import javax.swing.*;
import org.openide.cookies.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

/**
 *  Applies the JRefactory pretty printer to the currently selected editor. Will
 *  only be applied if only one editor is selected.
 */
public class PrettyPrinterAction extends CookieAction implements Presenter.Menu {

    public JMenuItem getMenuPresenter() {
        JMenuItem item = new JMenuItem(getName());
        item.addActionListener(this);
        return item;
    }

    public String getName() {
        return NbBundle.getMessage(PrettyPrinterAction.class,
                "LBL_PrettyPrinterAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // (PENDING) context help
        // return new HelpCtx (PrettyPrinterAction.class);
    }

    protected Class[] cookieClasses() {
        return new Class[]{EditorCookie.class};
    }

    /**
     * @return  MODE_EXACTLY_ONE
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected void performAction(Node[] nodes) {
        EditorCookie cookie =
                (EditorCookie) nodes[0].getCookie(EditorCookie.class);
//(PENDING) check for null editor pane
        NetBeansPrettyPrinter prettyPrinter = new NetBeansPrettyPrinter(cookie);
        prettyPrinter.prettyPrintCurrentWindow();
    }

    protected String iconResource() {
        return null;
    }

    /**
     *  Perform special enablement check in addition to the normal one.
     */
    protected boolean enable(Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }
        // Any additional checks ...
        return true;
    }

    /**
     *  Perform extra initialization of this action's singleton. PLEASE do not
     *  use constructors for this purpose!
     */
    protected void initialize() {
        super.initialize();
        putProperty(PrettyPrinterAction.SHORT_DESCRIPTION,
                NbBundle.getMessage(PrettyPrinterAction.class,
                "HINT_PrettyPrinterAction"));
    }

}
