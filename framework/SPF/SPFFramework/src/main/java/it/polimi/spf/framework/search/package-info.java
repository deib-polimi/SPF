/**
 * Package that contains implementation of the Search API of SPF.
 * 
 * Visible classes:
 * <ul>
 * <li> {@link it.polimi.spf.framework.search.SPFSearchManager}: manages ongoing searches and notification of search result </li>
 * <li> {@link it.polimi.spf.framework.search.SearchResult}: Container for people found in proximity by the middleware</li>
 * </ul>
 * 
 * TODO #SearchRefator: Refactor query handling into a unique component that can be used both by search responder and by trigger engine
 */
package it.polimi.spf.framework.search;