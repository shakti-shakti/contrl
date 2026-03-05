package com.family.parentalcontrol.api;

import com.family.parentalcontrol.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * Supabase PostgreSQL REST API Interface
 * Retrofit API interface for all Supabase database calls
 */
public interface SupabaseApi {

    // ============ PROFILES ============
    @POST("rest/v1/profiles")
    Call<User> createUser(@Body User user);

    @GET("rest/v1/profiles")
    Call<List<User>> getUser(@Query("id") String userId);

    @PATCH("rest/v1/profiles")
    Call<User> updateUser(@Query("id") String userId, @Body User user);

    // ============ RELATIONSHIPS (Parent-Child Pairing) ============
    @POST("rest/v1/relationships")
    Call<Map<String, Object>> createRelationship(@Body Map<String, Object> relationship);

    @GET("rest/v1/relationships")
    Call<List<Map<String, Object>>> getChildrenForParent(@Query("parent_id") String parentId);

    @GET("rest/v1/relationships")
    Call<List<Map<String, Object>>> getParentForChild(@Query("child_id") String childId);

    @PATCH("rest/v1/relationships")
    Call<Map<String, Object>> updateRelationship(@Query("id") String relationshipId, @Body Map<String, Object> data);

    // ============ LOCATIONS ============
    @POST("rest/v1/locations")
    Call<Location> saveLocation(@Body Location location);

    @GET("rest/v1/locations")
    Call<List<Location>> getLocationHistory(@Query("child_id") String childId, @Query("order") String order, @Query("limit") int limit);

    @GET("rest/v1/locations")
    Call<List<Location>> getLatestLocation(@Query("child_id") String childId, @Query("order") String order, @Query("limit") int limit);

    // ============ APP USAGE ============
    @POST("rest/v1/app_usage")
    Call<AppUsage> saveAppUsage(@Body AppUsage usage);

    @GET("rest/v1/app_usage")
    Call<List<AppUsage>> getAppUsageStats(@Query("child_id") String childId, @Query("order") String order);

    // ============ GEOFENCES ============
    @POST("rest/v1/geofences")
    Call<Geofence> createGeofence(@Body Geofence geofence);

    @GET("rest/v1/geofences")
    Call<List<Geofence>> getGeofences(@Query("child_id") String childId, @Query("is_active") String isActive);

    @PATCH("rest/v1/geofences")
    Call<Geofence> updateGeofence(@Query("id") String geofenceId, @Body Geofence geofence);

    @DELETE("rest/v1/geofences")
    Call<Void> deleteGeofence(@Query("id") String geofenceId);

    // ============ COMMANDS QUEUE ============
    @POST("rest/v1/commands")
    Call<Command> sendCommand(@Body Command command);

    @GET("rest/v1/commands")
    Call<List<Command>> getPendingCommands(@Query("child_id") String childId, @Query("status") String status, @Query("order") String order);

    @PATCH("rest/v1/commands")
    Call<Command> updateCommand(@Query("id") String commandId, @Body Map<String, Object> updates);

    // ============ NOTIFICATIONS LOG ============
    @POST("rest/v1/notifications_log")
    Call<Map<String, Object>> logNotification(@Body Map<String, Object> notification);

    @GET("rest/v1/notifications_log")
    Call<List<Map<String, Object>>> getNotifications(@Query("child_id") String childId, @Query("order") String order, @Query("limit") int limit);

    // ============ BLOCKED APPS ============
    @POST("rest/v1/blocked_apps")
    Call<Map<String, Object>> blockApp(@Body Map<String, Object> blockData);

    @GET("rest/v1/blocked_apps")
    Call<List<Map<String, Object>>> getBlockedApps(@Query("child_id") String childId);

    @DELETE("rest/v1/blocked_apps")
    Call<Void> unblockApp(@Query("id") String blockId);

    // ============ MEDIA ============
    @POST("rest/v1/media")
    Call<Map<String, Object>> saveMedia(@Body Map<String, Object> media);

    @GET("rest/v1/media")
    Call<List<Map<String, Object>>> getMedia(@Query("child_id") String childId, @Query("order") String order, @Query("limit") int limit);

    // ============ ALERTS ============
    @POST("rest/v1/alerts")
    Call<Map<String, Object>> createAlert(@Body Map<String, Object> alert);

    @GET("rest/v1/alerts")
    Call<List<Map<String, Object>>> getUnreadAlerts(@Query("child_id") String childId, @Query("is_read") String isRead);

    @PATCH("rest/v1/alerts")
    Call<Map<String, Object>> markAlertRead(@Query("id") String alertId);

    // ============ GENERIC QUERY ============
    @Headers("Content-Type: application/json")
    @GET("rest/v1/{table}")
    Call<List<Map<String, Object>>> query(@Path("table") String table, @QueryMap Map<String, String> filters);
}
