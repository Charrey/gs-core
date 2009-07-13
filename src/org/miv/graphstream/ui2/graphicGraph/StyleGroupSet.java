/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.graphstream.ui2.graphicGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.Rule;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.Selector;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.StyleSheet;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.StyleSheetListener;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.StyleConstants.ShadowMode;

/**
 * A set of style groups.
 * 
 * <p>
 * This class is in charge or storing all the style groups and to update them. Each time
 * an element is added or removed the groups are updated. Each time the style sheet changes
 * the groups are updated. 
 * </p>
 * 
 * @author Antoine Dutot
 */
public class StyleGroupSet implements StyleSheetListener
{
// Attribute
	
	/**
	 * The style sheet.
	 */
	protected StyleSheet stylesheet;
	
	/**
	 * All the groups indexed by their unique identifier.
	 */
	protected HashMap<String,StyleGroup> groups = new HashMap<String,StyleGroup>();
	
	/**
	 * Allows to retrieve the group containing a node knowing the node id.
	 */
	protected HashMap<String,String> byNodeIdGroups = new HashMap<String,String>();
	
	/**
	 * Allows to retrieve the group containing an edge knowing the node id.
	 */
	protected HashMap<String,String> byEdgeIdGroups = new HashMap<String,String>();
	
	/**
	 * Allows to retrieve the group containing a sprite knowing the node id.
	 */
	protected HashMap<String,String> bySpriteIdGroups = new HashMap<String,String>();
	
	/**
	 * Allows to retrieve the group containing a graph knowing the node id.
	 */
	protected HashMap<String,String> byGraphIdGroups = new HashMap<String,String>();
	
	/**
	 * The set of events actually occurring.
	 */
	protected EventSet eventSet = new EventSet();
	
	/**
	 * The groups sorted by their Z index.
	 */
	protected ZIndex zIndex = new ZIndex();
	
	/**
	 * Set of groups that cast shadow.
	 */
	protected ShadowSet shadow = new ShadowSet();
	
	/**
	 * Remove groups if they become empty?. 
	 */
	protected boolean removeEmptyGroups = true;
	
	/**
	 * Set of listeners.
	 */
	protected ArrayList<StyleGroupListener> listeners = new ArrayList<StyleGroupListener>();
	
// Construction
	
	/**
	 * New empty style group set, using the given style sheet to create style groups. The group set
	 * installs itself as a listener of the style sheet. So in order to completely stop using such
	 * a group, you must call {@link #release()}.
	 * @param stylesheet The style sheet to use to create groups.
	 */
	public StyleGroupSet( StyleSheet stylesheet )
	{
		this.stylesheet = stylesheet;

		stylesheet.addListener( this );
	}
	
// Access

	/**
	 * Number of groups.
	 * @return The number of groups.
	 */
	public int getGroupCount()
	{
		return groups.size();
	}
	
	/**
	 * Return a group by its unique identifier. The way group identifier are constructed reflects
	 * their contents.
	 * @param groupId The group identifier.
	 * @return The corresponding group or null if not found.
	 */
	public StyleGroup getGroup( String groupId )
	{
		return groups.get( groupId );
	}
	
	/**
	 * Iterator on the set of groups in no particular order.
	 * @return An iterator on the group set.
	 */
	public Iterator<? extends StyleGroup> getGroupIterator()
	{
		return groups.values().iterator();
	}
	
	/**
	 * Iterable set of groups elements, in no particular order.
	 * @return An iterable on the set of groups.
	 */
	public Iterable<? extends StyleGroup> groups()
	{
		return groups.values();
	}
	
	/**
	 * Iterator on the Z index.
	 * @return The z index iterator.
	 */
	public Iterator<HashSet<StyleGroup>> getZIterator()
	{
		return zIndex.getIterator();
	}
	
	/**
	 * Iterable set of "subsets of groups" sorted by Z level. Each subset of groups is at the
	 * same Z level.
	 * @return The z levels.
	 */
	public Iterable<HashSet<StyleGroup>> zIndex()
	{
		return zIndex;
	}
	
	/**
	 * Iterator on the style groups that cast a shadow.
	 * @return The shadow groups iterator.
	 */
	public Iterator<StyleGroup> getShadowIterator()
	{
		return shadow.getIterator();
	}
	
	/**
	 * Iterable set of groups that cast shadow.
	 * @return All the groups that cast a shadow.
	 */
	public Iterable<StyleGroup> shadows()
	{
		return shadow;
	}
	
	/**
	 * True if the set contains and styles the node whose identifier is given.
	 * @param id The node identifier.
	 * @return True if the node is in this set.
	 */
	public boolean containsNode( String id )
	{
		return byNodeIdGroups.containsKey( id );
	}
	
	/**
	 * True if the set contains and styles the edge whose identifier is given.
	 * @param id The edge identifier.
	 * @return True if the edge is in this set.
	 */
	public boolean containsEdge( String id )
	{
		return byEdgeIdGroups.containsKey( id );
	}
	
	/**
	 * True if the set contains and styles the sprite whose identifier is given.
	 * @param id The sprite identifier.
	 * @return True if the sprite is in this set.
	 */
	public boolean containsSprite( String id )
	{
		return bySpriteIdGroups.containsKey( id );
	}
	
	/**
	 * True if the set contains and styles the graph whose identifier is given.
	 * @param id The graph identifier.
	 * @return True if the graph is in this set.
	 */
	public boolean containsGraph( String id )
	{
		return byGraphIdGroups.containsKey( id );
	}
	
	/**
	 * Get an element.
	 * @param id The element id.
	 * @param elt2grp The kind of element.
	 * @return The element or null if not found.
	 */
	protected Element getElement( String id, HashMap<String,String> elt2grp )
	{
		String gid = elt2grp.get( id );
		
		if( gid != null )
		{
			StyleGroup group = groups.get( gid );
			return group.getElement( id );
		}

		return null;
	}

	/**
	 * Get a node element knowing its identifier.
	 * @param id The node identifier.
	 * @return The node if it is in this set, else null.
	 */
	public Node getNode( String id )
	{
		return (Node) getElement( id, byNodeIdGroups );
	}

	/**
	 * Get an edge element knowing its identifier.
	 * @param id The edge identifier.
	 * @return The edge if it is in this set, else null.
	 */
	public Edge getEdge( String id )
	{
		return (Edge) getElement( id, byEdgeIdGroups );
	}

	/**
	 * Get a sprite element knowing its identifier.
	 * @param id The sprite identifier.
	 * @return The sprite if it is in this set, else null.
	 */
	public GraphicSprite getSprite( String id )
	{
		return (GraphicSprite) getElement( id, bySpriteIdGroups );
	}

	/**
	 * Get a graph element knowing its identifier.
	 * @param id The graph identifier.
	 * @return The graph if it is in this set, else null.
	 */
	public Graph getGraph( String id )
	{
		return (Graph) getElement( id, byGraphIdGroups );
	}
	
	/**
	 * The number of nodes referenced.
	 * @return The node count.
	 */
	public int getNodeCount()
	{
		return byNodeIdGroups.size();
	}
	
	/**
	 * The number of edges referenced.
	 * @return The edge count.
	 */
	public int getEdgeCount()
	{
		return byEdgeIdGroups.size();
	}
	
	/**
	 * The number of sprites referenced.
	 * @return The sprite count.
	 */
	public int getSpriteCount()
	{
		return bySpriteIdGroups.size();
	}
	
	/**
	 * Iterator on the set of nodes.
	 * @return An iterator on all node elements contained in style groups.
	 */
	public Iterator<? extends Node> getNodeIterator()
	{
		return new ElementIterator<Node>( byNodeIdGroups );
	}
	
	/**
	 * Iterator on the set of edges.
	 * @return An iterator on all edge elements contained in style groups.
	 */
	public Iterator<? extends Edge> getEdgeIterator()
	{
		return new ElementIterator<Edge>( byEdgeIdGroups );
	}
	
	/**
	 * Iterator on the set of sprite.
	 * @return An iterator on all sprite elements contained in style groups.
	 */
	public Iterator<? extends GraphicSprite> getSpriteIterator()
	{
		return new ElementIterator<GraphicSprite>( bySpriteIdGroups );
	}
	
	/**
	 * Retrieve the group identifier of an element knowing the element identifier.
	 * @param element The element to search for.
	 * @return Identifier of the group containing the element.
	 */
	public String getElementGroup( Element element )
	{
		if( element instanceof Node )
		{
			return byNodeIdGroups.get( element.getId() );
		}
		else if( element instanceof Edge )
		{
			return byEdgeIdGroups.get( element.getId() );
		}
		else if( element instanceof GraphicSprite )
		{
			return bySpriteIdGroups.get( element.getId() );
		}
		else if( element instanceof Graph )
		{
			return byGraphIdGroups.get( element.getId() );
		}
		else
		{
			throw new RuntimeException( "What ?" );
		}
	}
	
	/**
	 * Get the style of an element.
	 * @param element The element to search for.
	 * @return The style group of the element (which is also a style).
	 */
	public StyleGroup getStyleForElement( Element element )
	{
		String gid = getElementGroup( element );
		
		return groups.get( gid );
	}
	
	/**
	 * Get the style of a given node.
	 * @param node The node to search for.
	 * @return The node style.
	 */
	public StyleGroup getStyleFor( Node node )
	{
		String gid = byNodeIdGroups.get( node.getId() );
		return groups.get( gid );
	}
	
	/**
	 * Get the style of a given edge.
	 * @param edge The edge to search for.
	 * @return The edge style.
	 */
	public StyleGroup getStyleFor( Edge edge )
	{
		String gid = byEdgeIdGroups.get( edge.getId() );
		return groups.get( gid );
	}
	
	/**
	 * Get the style of a given sprite.
	 * @param sprite The node to search for.
	 * @return The sprite style.
	 */
	public StyleGroup getStyleFor( GraphicSprite sprite )
	{
		String gid = bySpriteIdGroups.get( sprite.getId() );
		return groups.get( gid );
	}
	
	/**
	 * Get the style of a given graph.
	 * @param graph The node to search for.
	 * @return The graph style.
	 */
	public StyleGroup getStyleFor( Graph graph )
	{
		String gid = byGraphIdGroups.get( graph.getId() );
		return groups.get( gid );
	}
	
	/**
	 * True if groups are removed when becoming empty. This setting allows to keep empty group when
	 * the set of elements is quite dynamic. This allows to avoid recreting groups when an element
	 * appears and disappears regularly.
	 * @return True if the groups are removed when empty.
	 */
	public boolean areEmptyGroupRemoved()
	{
		return removeEmptyGroups;
	}

	/**
	 * The Z index object.
	 * @return The Z index.
	 */
	public ZIndex getZIndex()
	{
		return zIndex;
	}
	
	/**
	 * The set of style groups that cast a shadow.
	 * @return The set of shadowed style groups.
	 */
	public ShadowSet getShadowSet()
	{
		return shadow;
	}
	
// Command

	/**
	 * Release any dependency to the style sheet.
	 */
	public void release()
	{
		stylesheet.removeListener( this );
	}
	
	/**
	 * Empties this style group set.
	 * The style sheet is listener is not removed, use {@link #release()} to do that.
	 */
	public void clear()
	{
		byEdgeIdGroups.clear();
		byNodeIdGroups.clear();
		bySpriteIdGroups.clear();
		byGraphIdGroups.clear();
		groups.clear();
		zIndex.clear();
		shadow.clear();
	}
	
	/**
	 * Remove or keep groups that becomes empty, if true the groups are removed. If this setting
	 * was set to false, and is now true, the group set is purged of the empty groups.
	 * @param on If true the groups will be removed.
	 */
	public void setRemoveEmptyGroups( boolean on )
	{
		if( removeEmptyGroups == false && on == true )
		{
			Iterator<?extends StyleGroup> i = groups.values().iterator(); 

			while( i.hasNext() )
			{
				StyleGroup g = i.next();
				
				if( g.isEmpty() )
					i.remove();
			}
		}

		removeEmptyGroups = on;
	}
	
	protected StyleGroup addGroup( String id, ArrayList<Rule> rules, Element firstElement )
	{
		StyleGroup group = new StyleGroup( id, rules, firstElement, eventSet );

		groups.put( id, group );
		zIndex.groupAdded( group );
		shadow.groupAdded( group );
		
		return group;
	}
	
	protected void removeGroup( StyleGroup group )
	{
		zIndex.groupRemoved( group );
		shadow.groupRemoved( group );
		groups.remove( group.getId() );
		group.release();
	}

	/**
	 * Add an element and bind it to its style group. The group is created if needed.
	 * @param element The element to add.
	 * @return The style group where the element was added.
	 */
	public StyleGroup addElement( Element element )
	{
		ArrayList<Rule> rules = stylesheet.getRulesFor( element );
		String          gid   = stylesheet.getStyleGroupIdFor( element, rules );
		StyleGroup      group = groups.get( gid );
		
		if( group == null )
		     addGroup( gid, rules, element );
		else group.addElement( element );
		
		addElementToReverseSearch( element, gid );
		
		for( StyleGroupListener listener: listeners )
			listener.elementStyleChanged( element, group );
		
		return group;
	}
	
	/**
	 * Remove an element from the group set. If the group becomes empty after the element removal,
	 * depending on the setting of {@link #areEmptyGroupRemoved()}, the group is deleted or kept.
	 * Keeping groups allows to handle faster elements that constantly appear and disappear. 
	 * @param element The element to remove.
	 */
	public void removeElement( Element element )
	{
		String     gid   = getElementGroup( element );
		StyleGroup group = groups.get( gid );
		
		if( group != null )
		{
			group.removeElement( element );
			removeElementFromReverseSearch( element );
			
			if( removeEmptyGroups && group.isEmpty() )
				removeGroup( group );
		}
	}
	
	protected void addElementToReverseSearch( Element element, String groupId )
	{
		if( element instanceof Node )
		{
			byNodeIdGroups.put( element.getId(), groupId );
		}
		else if( element instanceof Edge )
		{
			byEdgeIdGroups.put( element.getId(), groupId );
		}
		else if( element instanceof GraphicSprite )
		{
			bySpriteIdGroups.put( element.getId(), groupId );
		}
		else if( element instanceof Graph )
		{
			byGraphIdGroups.put( element.getId(), groupId );
		}
		else
		{
			throw new RuntimeException( "What ?" );
		}
	}
	
	protected void removeElementFromReverseSearch( Element element )
	{
		if( element instanceof Node )
		{
			byNodeIdGroups.remove( element.getId() );
		}
		else if( element instanceof Edge )
		{
			byEdgeIdGroups.remove( element.getId() );
		}
		else if( element instanceof GraphicSprite )
		{
			bySpriteIdGroups.remove( element.getId() );
		}
		else if( element instanceof Graph )
		{
			byGraphIdGroups.remove( element.getId() );
		}
		else
		{
			throw new RuntimeException( "What ?" );
		}
	}
	
	/**
	 * Push an event on the event stack. Events trigger the replacement of a style by an alternative
	 * style (or meta-class) when possible. If an event is on the event stack, each time a style has
	 * an alternative corresponding to the event, the alternative is used instead of the style.
	 * @param event The event to push.
	 */
	public void pushEvent( String event )
	{
		eventSet.pushEvent( event );
	}
	
	/**
	 * Pop an event from the event set.
	 * @param event The event to remove.
	 */
	public void popEvent( String event )
	{
		eventSet.popEvent( event );
	}
	
	/**
	 * Add a listener for element style changes.
	 * @param listener The listener to add.
	 */
	public void addListener( StyleGroupListener listener )
	{
		listeners.add( listener );
	}
	
	/**
	 * Remove a style change listener.
	 * @param listener The listener to remove.
	 */
	public void removeListener( StyleGroupListener listener )
	{
		int index = listeners.lastIndexOf( listener );
		
		if( index >= 0 )
		{
			listeners.remove( index );
		}
	}
	
// Listener -- What to do when a change occurs in the style sheet.
	
	public void styleAdded( Rule oldRule, Rule newRule )
    {
	    // When a style change, we need to update groups.
		// Several cases :
		//		1. The style already exists
		// 			* Nothing to do in fact. All the elements are still in place.
		//			  No style rule (selectors) changed, and therefore we do not have
		//			  to change the groups since they are built using the selectors.
		//		2. The style is new
		//			* we need to check all the groups concerning this kind of element (we can
		//			  restrict our search to these groups, since other will not be impacted),
		//			  and check all elements of these groups.
		
		if( oldRule == null )
		     checkForNewStyle( newRule ); // no need to check Z and shadow, done when adding/changing group.
		else checkZIndexAndShadow( oldRule, newRule );
    }
	
	/**
	 * Check each group that may have changed, for example to rebuild the Z index and the shadow
	 * set.
	 * @param oldRule The old rule that changed.
	 * @param newRule The new rule that participated in the change.
	 */
	protected void checkZIndexAndShadow( Rule oldRule, Rule newRule )
	{
		if( oldRule != null )
		{
			if( oldRule.selector.getId() != null || oldRule.selector.getClazz() != null )
			{
				// We may accelerate things a bit when a class or id style is modified,
				// since only the groups listed in the style are concerned (we are at the
				// bottom of the inheritance tree).
				
				for( String s: oldRule.getGroups() )
				{
					StyleGroup group = groups.get( s );
					zIndex.groupChanged( group );
					shadow.groupChanged( group );
				}
			}
			else
			{
				// For kind styles "NODE", "EDGE", "GRAPH", "SPRITE", we must reset
				// the whole Z and shadows for the kind, since several styles may
				// have changed.
				
				Selector.Type type = oldRule.selector.type;
				
				for( StyleGroup group: groups.values() )
				{
					if( group.getType() == type )
					{
						zIndex.groupChanged( group );
						shadow.groupChanged( group );
					}
				}
			}
		}
	}
	
	/** We try to avoid at most to affect anew styles to elements and to recreate groups, which
	 * is time consuming.
	 * 
	 * Two cases :
	 *	1. The style is an specific (id) style. In this case a new group may be added.
	 *		* check an element matches the style and in this case create the group by
	 *			adding the element.
	 *		* else do nothing.
	 *	2. The style is a kind or class style.
	 *		* check all the groups in the kind of the style (graph, node, edge, sprite) and only
	 *		  in this kind (since other will never be affected).
	 *			* remove all groups of this kind.
	 *			* add all elements of this kind anew to recreate the group.
	 */
	protected void checkForNewStyle( Rule newRule )
	{
		switch( newRule.selector.type )
		{
			case GRAPH:
				if( newRule.selector.getId() != null )
				     checkForNewIdStyle( newRule, byGraphIdGroups );
				else checkForNewStyle( newRule, byGraphIdGroups );
				break;
			case NODE:
				if( newRule.selector.getId() != null )
				     checkForNewIdStyle( newRule, byNodeIdGroups );
				else checkForNewStyle( newRule, byNodeIdGroups );
				break;
			case EDGE:
				if( newRule.selector.getId() != null )
				     checkForNewIdStyle( newRule, byEdgeIdGroups );
				else checkForNewStyle( newRule, byEdgeIdGroups );
				break;
			case SPRITE:
				if( newRule.selector.getId() != null )
				     checkForNewIdStyle( newRule, bySpriteIdGroups );
				else checkForNewStyle( newRule, bySpriteIdGroups );
				break;
			case ANY:
			default:
				throw new RuntimeException( "What ?" );
		}
	}

	/**
	 * Check for a new specific style (applies only to one element). 
	 * @param newRule The new style rule.
	 * @param elt2grp The name space.
	 */
	protected void checkForNewIdStyle( Rule newRule, HashMap<String,String> elt2grp )
	{
		// There is only one element that matches the identifier.
		
		Element element = getElement( newRule.selector.getId(), elt2grp );
		
		if( element != null )
		{
			removeElement( element );	// Remove the element from its old group. Potentially delete a group.
			addElement( element );		// Add the element to its new own group (since this is an ID style).
		}
	}

	/**
	 * Check for a new kind or class style in a given name space (node, edge, sprite, graph).
	 * @param newRule The new style rule.
	 * @param elt2grp The name space.
	 */
	protected void checkForNewStyle( Rule newRule, HashMap<String,String> elt2grp )
	{
		ArrayList<Element> elementsToCheck = new ArrayList<Element>();
		
		for( String eltId: elt2grp.keySet() )
			elementsToCheck.add( getElement( eltId, elt2grp ) );
		
		for( Element element: elementsToCheck )
		{
			removeElement( element );
			addElement( element );
		}
	}
	
// Utility

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append( String.format( "Style groups (%d) :%n", groups.size() ) );
		
		for( StyleGroup group: groups.values() )
		{
			builder.append( group.toString( 1 ) );
			builder.append( String.format( "%n" ) );
		}
		
		return builder.toString();
	}
	
// Inner classes

/**
 * Set of events (meta-classes) actually active.
 * 
 * <p>
 * The event set contains the set of events actually occurring. This is used to select alternate
 * styles. The events actually occurring are in precedence order. The last one is the most
 * important.
 * </p>
 * 
 * @author Antoine Dutot
 */
public class EventSet
{
	public ArrayList<String> eventSet = new ArrayList<String>();
	
	public String events[] = new String[0];
	
	/**
	 * Add an event to the set.
	 * @param event The event to add.
	 */
	public void pushEvent( String event )
	{
		eventSet.add( event );
		events = eventSet.toArray( events );
	}
	
	/**
	 * Remove an event from the set.
	 * @param event The event to remove.
	 */
	public void popEvent( String event )
	{
		int index = eventSet.lastIndexOf( event );
		
		if( index >= 0 )
			eventSet.remove( index );
		
		events = eventSet.toArray( events );
	}
	
	/**
	 * The set of events in order, the most important at the end.
	 * @return The event set.
	 */
	public String[] getEvents()
	{
		return events;
	}
}

/**
 * All the style groups sorted by their Z index.
 * 
 * <p>
 * This structure is maintained by each time a group is added or removed, or when the style of
 * a group changed.
 * </p>
 * 
 * @author Antoine Dutot
 */
public class ZIndex implements Iterable<HashSet<StyleGroup>>
{
	/**
	 * Ordered set of groups.
	 */
	public ArrayList<HashSet<StyleGroup>> zIndex = new ArrayList<HashSet<StyleGroup>>();
	
	/**
	 * Knowing a group, tell if its Z index.
	 */
	public HashMap<String,Integer> reverseZIndex = new HashMap<String,Integer>();
	
	/**
	 * New empty Z index.
	 */
	public ZIndex()
	{
		zIndex.ensureCapacity( 256 );
		
		for( int i=0; i<256; i++ )
			zIndex.add( null );
	}
	
	/**
	 * Iterator on the set of Z index cells. Each item is a set of style groups that
	 * pertain to the same Z index. 
	 * @return Iterator on the Z index.
	 */
	protected Iterator<HashSet<StyleGroup>> getIterator()
	{
		return new ZIndexIterator();
	}

	public Iterator<HashSet<StyleGroup>> iterator()
	{
		return getIterator();
	}
	
	/**
	 * A new group appeared, put it in the z index.
	 * @param group The group to add.
	 */
	protected void groupAdded( StyleGroup group )
	{
		int z = convertZ( group.getZIndex() );

		if( zIndex.get( z ) == null )
			zIndex.set( z, new HashSet<StyleGroup>() );
		
		zIndex.get(z).add( group );
		reverseZIndex.put( group.getId(), z );
	}
	
	/**
	 * A group eventually changed, check its location.
	 * @param group The group to check.
	 */
	protected void groupChanged( StyleGroup group )
	{
		int oldZ = reverseZIndex.get( group.getId() );
		int newZ = convertZ( group.getZIndex() );
		
		if( oldZ != newZ )
		{
			HashSet<StyleGroup> map = zIndex.get( oldZ );
			
			if( map != null )
			{
				map.remove( group );
				reverseZIndex.remove( group.getId() );

				if( map.isEmpty() )
					zIndex.set( oldZ, null );
			}
			
			groupAdded( group );
		}
	}
	
	/**
	 * A group was removed, remove it from the Z index.
	 * @param group The group to remove.
	 */
	protected void groupRemoved( StyleGroup group )
	{
		int z = convertZ( group.getZIndex() );
		
		HashSet<StyleGroup> map = zIndex.get( z );
		
		if( map != null )
		{
			map.remove( group );
			reverseZIndex.remove( group.getId() );
			
			if( map.isEmpty() )
				zIndex.set( z, null );
		}
		else
		{
			throw new RuntimeException( "Inconsistency in Z-index" );
		}
	}
	
	public void clear()
	{
		zIndex.clear();
		reverseZIndex.clear();
	}

	/**
	 * Convert a [-127,127] value into a [0,255] value and check bounds.
	 * @param z The Z value to convert.
	 * @return The Z value converted and bounded to [0,255].
	 */
	protected int convertZ( int z )
	{
		z += 127;
		
		if( z < 0 )
			z = 0;
		else if( z > 255 )
			z = 255;
		
		return z;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( String.format( "Z index :%n" ) );
		
		for( int i=0; i<256; i++ )
		{
			if( zIndex.get( i ) != null )
			{
				sb.append( String.format( "    * %d -> ", i-127 ) );
				
				HashSet<StyleGroup> map = zIndex.get( i );
				
				for( StyleGroup g: map )
					sb.append( String.format( "%s ", g.getId() ) );
				
				sb.append( String.format( "%n" ) );
			}
		}
		
		return sb.toString();
	}
	
	public class ZIndexIterator implements Iterator<HashSet<StyleGroup>>
	{
		public int index = 0;

		public ZIndexIterator()
		{
			zapUntilACell();
		}
		
		protected void zapUntilACell()
		{
			while( index < 256 && zIndex.get( index ) == null )
				index++;
		}
		
		public boolean hasNext()
        {
			return( index < 256 );
        }

		public HashSet<StyleGroup> next()
        {
			if( hasNext() )
			{
				HashSet<StyleGroup> cell = zIndex.get( index );
				index++;
				zapUntilACell();
				return cell;
			}

	        return null;
        }

		public void remove()
        {
			throw new RuntimeException( "This iterator does not support removal." );
        }
	}
}

/**
 * Set of groups that cast a shadow.
 * 
 * @author Antoine Dutot
 */
public class ShadowSet implements Iterable<StyleGroup>
{
	/**
	 * The set of groups casting shadow.
	 */
	protected HashSet<StyleGroup> shadowSet = new HashSet<StyleGroup>();
	
	/**
	 * Iterator on the set of groups that cast a shadow.
	 * @return An iterator on the shadow style group set.
	 */
	protected Iterator<StyleGroup> getIterator()
	{
		return shadowSet.iterator();
	}

	public Iterator<StyleGroup> iterator()
	{
		return getIterator();
	}
	
	/**
	 * A group appeared, check its shadow status.
	 * @param group The group added.
	 */
	protected void groupAdded( StyleGroup group )
	{
		if( group.getShadowMode() != ShadowMode.NONE )
			shadowSet.add( group );
	}

	/**
	 * A group eventually changed, check its shadow status.
	 * @param group The group that changed.
	 */
	protected void groupChanged( StyleGroup group )
	{
		if( group.getShadowMode() == ShadowMode.NONE )
		     shadowSet.remove( group );
		else shadowSet.add( group );
	}
	
	/**
	 * A group was removed, remove it from the shadow if needed.
	 * @param group The group removed.
	 */
	protected void groupRemoved( StyleGroup group )
	{
		// Faster than to first test its existence or shadow status :

		shadowSet.remove( group );
	}
	
	protected void clear()
	{
		shadowSet.clear();
	}
}

/**
 * Iterator that allows to browse all graph elements of a given kind (nodes, edges, sprites, graphs)
 * as if they where in a single set, whereas they are in style groups.
 * 
 * @author Antoine Dutot
 * @param <E> The kind of graph element.
 */
protected class ElementIterator<E extends Element> implements Iterator<E>
{
	protected HashMap<String,String> elt2grp;
	
	protected Iterator<String> elts;
	
	public ElementIterator( HashMap<String,String> elements2groups )
	{
		elt2grp = elements2groups;
		elts    = elements2groups.keySet().iterator();
	}

	public boolean hasNext()
    {
        return elts.hasNext();
    }

	@SuppressWarnings("unchecked")
    public E next()
    {
		String     eid = elts.next();
		String     gid = elt2grp.get( eid );
		StyleGroup grp = groups.get( gid );
		
        return (E) grp.getElement( eid );
    }

	public void remove()
    {
		throw new RuntimeException( "remove not implemented in this iterator" );
    }
}
}