package edu.fau.communityupgrade.helper;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.models.User;
import edu.fau.communityupgrade.models.Vote;

/**
 * This Helper class is used to convert ParseObjects 
 * to Object Models.
 * @author kyle
 *
 */
public class ParseHelper {

	private static final String TAG = "ParseHelper";
	
	/**
	 * Converts ParseUser to User Model Object
	 * @param parseUser
	 * @return
	 */
	public static User parseUserToUser(final ParseUser parseUser)
	{
		
		User user = new User(parseUser.getString(UserManager.USERNAME),
				parseUser.getObjectId());	
		return user;
	}
	
	public static Vote parseObjectToVote(final ParseObject parseObject)
	{
		if(parseObject == null)
			return null;
		
		String objectId = parseObject.getObjectId();
		String commentId = parseObject.getString(CommentManager.VOTE_COMMENT_ID);
		String userId = parseObject.getString(CommentManager.VOTE_USER_ID);
		boolean isUpvote = parseObject.getBoolean(CommentManager.VOTE_IS_UPVOTE);
		
		return new Vote(objectId,userId,commentId,isUpvote);
	}
	
	public static Comment parseObjectToComment(final ParseObject parseObject)
	{
		return parseObjectToComment(parseObject,null);
	}
	
	/**
	 * Converts ParseObject to Comment Model Object
	 * @param parseObject
	 * @return
	 */
	public static Comment parseObjectToComment(final ParseObject parseObject, final ParseObject voteStatusObject)
	{
		String objectId = parseObject.getObjectId();
		String comment_content = parseObject.getString(CommentManager.COMMENT_CONTENT);
		String placeId = parseObject.getParseObject(CommentManager.PLACE_ID).getObjectId();
		double score = parseObject.getDouble(CommentManager.SCORE);
		User createdBy = null;
		
		ParseUser user = parseObject.getParseUser(CommentManager.CREATED_BY);
		if(user!= null)
		{
			createdBy = parseUserToUser(user);
		}
			
		
		String parentId = null;
		
		if(parseObject.containsKey(CommentManager.PARENT_ID)){
			parentId = parseObject.getString(CommentManager.PARENT_ID);
		}
		
		Vote vote = parseObjectToVote(voteStatusObject);
		
		Comment comment = new Comment(objectId,comment_content,placeId,createdBy,parentId,score,vote);
		return comment;
	}
	
	/**
	 * Converts a ParseObject to a Place Model Object
	 * @param parseObject
	 * @return
	 */
	public static Place parseObjectToPlace(final ParseObject parseObject)
	{
		ParseUser parseUser = parseObject.getParseUser(PlaceManager.CREATED_BY);
		User user = parseUserToUser(parseUser);
		
		
		String name = parseObject.getString(PlaceManager.NAME); 
		String description = parseObject.getString(PlaceManager.DESCRIPTION);
		String address = parseObject.getString(PlaceManager.ADDRESS);
		String cNumber = parseObject.getString(PlaceManager.CONTACT_NUMBER);
		String objectId = parseObject.getObjectId();
		
		ParseGeoPoint point = parseObject.getParseGeoPoint(PlaceManager.LOCATION);
		
		
		ParseRelation<ParseObject> relation = parseObject.getRelation(CommentManager.TABLE);
		ParseQuery<ParseObject> query = relation.getQuery();
		
		
		
		//Create Place
		Place place = new Place(objectId,name,user,description,cNumber,address,point.getLatitude(),point.getLongitude(),null);
		
		return place;
	}
	
	
	
}
