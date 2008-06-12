package org.dcache.services.info.gathers;

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dcache.services.info.base.BooleanStateValue;
import org.dcache.services.info.base.IntegerStateValue;
import org.dcache.services.info.base.State;
import org.dcache.services.info.base.StateComposite;
import org.dcache.services.info.base.StatePath;
import org.dcache.services.info.base.StateUpdate;
import org.dcache.services.info.base.StringStateValue;

import diskCacheV111.services.space.LinkGroup;
import diskCacheV111.services.space.message.GetLinkGroupsMessage;
import diskCacheV111.util.VOInfo;
import diskCacheV111.vehicles.Message;

public class LinkgroupDetailsMsgHandler implements MessageHandler {

	private static Logger _log = Logger.getLogger( LinkgroupDetailsMsgHandler.class);
	private static final StatePath LINKGROUPS_PATH = new StatePath("linkgroups");
	private static final String WILDCARD_ROLE = "*";
	
	private State _state = State.getInstance();

	public boolean handleMessage(Message messagePayload, long metricLifetime) {
		
		if( !(messagePayload instanceof GetLinkGroupsMessage))
			return false;

		if( _log.isInfoEnabled())
			_log.info( "received linkgroup details msg.");

		GetLinkGroupsMessage msg = (GetLinkGroupsMessage) messagePayload;

		Set<LinkGroup> linkGroups = msg.getLinkGroupSet();

		if( linkGroups.size() == 0) {
			_log.info( "received GetLinkGroupNamesMessage with no linkgroups listed");
			return true;
		}
		
		StateUpdate update = new StateUpdate();

		for( LinkGroup linkGroup : linkGroups) {
			StatePath thisLinkGroupPath = LINKGROUPS_PATH.newChild( String.valueOf(linkGroup.getId()));
			
			update.appendUpdate( thisLinkGroupPath.newChild("name"), new StringStateValue( linkGroup.getName(), metricLifetime));
			update.appendUpdate( thisLinkGroupPath.newChild("id"), new IntegerStateValue( linkGroup.getId(), metricLifetime));
			
			StatePath spacePath = thisLinkGroupPath.newChild( "space");
			update.appendUpdate( spacePath.newChild("free"), new IntegerStateValue( linkGroup.getFreeSpace(), metricLifetime));
			update.appendUpdate( spacePath.newChild("reserved"), new IntegerStateValue( linkGroup.getReservedSpaceInBytes(), metricLifetime));
			update.appendUpdate( spacePath.newChild("available"), new IntegerStateValue( linkGroup.getAvailableSpaceInBytes(), metricLifetime));
			
			StatePath accLatPath = thisLinkGroupPath.newChild("access-latency");
			update.appendUpdate( accLatPath.newChild("onlineAllowed"), new BooleanStateValue( linkGroup.isOnlineAllowed(), metricLifetime));
			update.appendUpdate( accLatPath.newChild("nearlineAllowed"), new BooleanStateValue( linkGroup.isNearlineAllowed(), metricLifetime));

			StatePath retPolPath = thisLinkGroupPath.newChild("retention-policy");
			update.appendUpdate( retPolPath.newChild("replicaAllowed"), new BooleanStateValue( linkGroup.isReplicaAllowed(), metricLifetime));
			update.appendUpdate( retPolPath.newChild("outputAllowed"), new BooleanStateValue( linkGroup.isOutputAllowed(), metricLifetime));
			update.appendUpdate( retPolPath.newChild("custodialAllowed"), new BooleanStateValue( linkGroup.isCustodialAllowed(), metricLifetime));

			Date lastUpdate = new Date( linkGroup.getUpdateTime());
			CellMessageHandlerSkel.addTimeMetrics( update, thisLinkGroupPath.newChild("lastUpdated"), lastUpdate, metricLifetime);

			StatePath vosPath = thisLinkGroupPath.newChild( "authorisation");			
			VOInfo voInfo[] = linkGroup.getVOs();

			if( voInfo.length > 0) { 
				for( int i = 0; i < voInfo.length; i++) {
					VOInfo thisVO = voInfo[i];
					addVoInfo( update, vosPath.newChild(thisVO.toString()), thisVO, metricLifetime);
				}
			} else {
				// Ensure the VOs branch exists.
				update.appendUpdate( vosPath, new StateComposite(metricLifetime));
			}
		}
			
		_state.updateState(update);
		
		return true;
	}

	/**
	 * Add VO-specific information about a linkgroup underneath the correct branch. 
	 * @param update the StateUpdate to append new metrics underneath
	 * @param vosPath the branch that metrics will be added underneath. 
	 * @param vo the VOInfo of this VO.
	 */
	private void addVoInfo( StateUpdate update, StatePath voPath, VOInfo vo, long metricLifetime) {
		String group = vo.getVoGroup();
		String role = vo.getVoRole();
		
		if( group != null) 
			update.appendUpdate( voPath.newChild( "group"), new StringStateValue( group, metricLifetime));
		
		if( role != null)
			update.appendUpdate( voPath.newChild( "role"), new StringStateValue( role, metricLifetime));

		if( group != null) {
			StringBuilder fqan = new StringBuilder();
		
			fqan.append( vo.getVoGroup());
			
			if( role != null && !role.equals( WILDCARD_ROLE)) {
				fqan.append( "/Role=");
				fqan.append( role);
			}

			update.appendUpdate( voPath.newChild( "FQAN"), new StringStateValue( fqan.toString(), metricLifetime));
		}
	}

}
