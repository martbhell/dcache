package org.dcache.restful.util;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.security.AccessController;
import java.util.Set;

import diskCacheV111.util.PnfsHandler;
import org.dcache.auth.attributes.LoginAttribute;
import org.dcache.auth.attributes.Restriction;
import org.dcache.auth.attributes.Restrictions;
import org.dcache.cells.CellStub;
import org.dcache.poolmanager.RemotePoolMonitor;
import org.dcache.restful.services.transfers.TransferInfoService;
import org.dcache.util.list.ListDirectoryHandler;

import static org.dcache.http.AuthenticationHandler.DCACHE_LOGIN_ATTRIBUTES;

public class ServletContextHandlerAttributes {
    public final static String DL = "org.dcache.restful";
    public final static String CS = "org.dcache.restful.CS";
    public final static String POOL_MANAGER = "org.dcache.restful.PoolManager";
    public final static String PM = "org.dcache.restful.PM";
    public final static String TF = "org.dcache.restful.TF";
    public final static String PinMngStub = "org.dcache.restful.PinMngStub";
    public final static String PathMapper = "org.dcache.restful.PathMapper";



    public static Subject getSubject()
    {
        return Subject.getSubject(AccessController.getContext());
    }

    public static Set<LoginAttribute> getLoginAttributes(HttpServletRequest request)
    {
        return (Set<LoginAttribute>) request.getAttribute(DCACHE_LOGIN_ATTRIBUTES);
    }

    public static Restriction getRestriction()
    {
        return Restrictions.readOnly();
    }

    public static ListDirectoryHandler getListDirectoryHandler(ServletContext ctx)
    {
        return (ListDirectoryHandler) (ctx.getAttribute(DL));
    }

    public static PnfsHandler getPnfsHandler(ServletContext ctx)
    {
        CellStub cellStub = (CellStub) (ctx.getAttribute(CS));
        PnfsHandler handler = new PnfsHandler(cellStub);
        handler.setSubject(getSubject());

        return handler;
    }

    public static RemotePoolMonitor getRemotePoolMonitor(ServletContext ctx)
    {
        return (RemotePoolMonitor) (ctx.getAttribute(PM));
    }

    public static CellStub getPinManager(ServletContext ctx)
    {
        CellStub cellStub = (CellStub) (ctx.getAttribute(PinMngStub));
        return cellStub;

    }

    public static PathMapper getPathMapper(ServletContext ctx)
    {
        PathMapper pathMapper = (PathMapper) (ctx.getAttribute(PathMapper));
        return pathMapper;

    }

    public static TransferInfoService getTransferInfoService(ServletContext ctx)
    {
        return (TransferInfoService) ctx.getAttribute(TF);
    }
    public static CellStub getPoolManger(ServletContext ctx)
    {
        CellStub cellStub = (CellStub) ctx.getAttribute(POOL_MANAGER);
        return cellStub;

    }


}
