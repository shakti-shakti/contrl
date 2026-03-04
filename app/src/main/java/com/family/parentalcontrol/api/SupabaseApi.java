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

    @GET("rest/v1/profiles?id=eq.{id}")
    Call<List<User>> getUser(@Path("id") String userId);

    @PATCH("rest/v1/profiles?id=eq.{id}")
    Call<User> updateUser(@Path("id") String userId, @Body User user);

    // ============ RELATIONSHIPS (Parent-Child Pairing) ============
    @POST("rest/v1/relationships")
    Call<Map<String, Object>> createRelationship(@Body Map<String, Object> relationship);

    @GET("rest/v1/relationships?parent_id=eq.{parentId}")
    Call<List<Map<String, Object>>> getChildrenForParent(@Path("parentId") String parentId);

    @GET("rest/v1/relationships?child_id=eq.{childId}")
    Call<List<Map<String, Object>>> getParentForChild(@Path("childId") String childId);

    @PATCH("rest/v1/relationships?id=eq.{id}")
    Call<Map<String, Object>> updateRelationship(@Path("id") String relationshipId, @Body Map<String, Object> data);

    // ============ LOCATIONS ============
    @POST("rest/v1/locations")
    Call<Location> saveLocation(@Body Location location);

    @GET("rest/v1/locations?child_id=eq.{childId}&order=timestamp.desc&limit={limit}")
    Call<List<Location>> getLocationHistory(@Path("childId") String childId, @Path("limit") int limit);

    @GET("rest/v1/locations?child_id=eq.{childId}&order=timestamp.desc&limit=1")
    Call<List<Location>> getLatestLocation(@Path("childId") String childId);

    // ============ APP USAGE ============
    @POST("rest/v1/app_usage")
    Call<AppUsage> saveAppUsage(@Body AppUsage usage);

    @GET("rest/v1/app_usage?child_id=eq.{childId}&order=usage_duration.desc")
    Call<List<AppUsage>> getAppUsageStats(@Path("childId") String childId);

    // ============ GEOFENCES ============
    @POST("rest/v1/geofences")
    Call<Geofence> createGeofence(@Body Geofence geofence);

    @GET("rest/v1/geofences?child_id=eq.{childId}&is_active=eq.true")
    Call<List<Geofence>> getGeofences(@Path("childId") String childId);

    @PATCH("rest/v1/geofences?id=eq.{id}")
    Call<Geofence> updateGeofence(@Path("id") String geofenceId, @Body Geofence geofence);

    @DELETE("rest/v1/geofences?id=eq.{id}")
    Call<Void> deleteGeofence(@Path("id") String geofenceId);

    // ============ COMMANDS QUEUE ============
    @POST("rest/v1/commands")
    Call<Command> sendCommand(@Body Command command);

    @GET("rest/v1/commands?child_id=eq.{childId}&status=eq.pending&order=created_at.asc")
    Call<List<Command>> getPendingCommands(@Path("childId") String childId);

    @PATCH("rest/v1/commands?id=eq.{id}")
    Call<Command> updateCommand(@Path("id") String commandId, @Body Map<String, Object> updates);

    // ============ NOTIFICATIONS LOG ============
    @POST("rest/v1/notifications_log")
    Call<Map<String, Object>> logNotification(@Body Map<String, Object> notification);

    @GET("rest/v1/notifications_log?child_id=eq.{childId}&order=sent_at.desc&limit={limit}")
    Call<List<Map<String, Object>>> getNotifications(@Path("childId") String childId, @Path("limit") int limit);

    // ============ BLOCKED APPS ============
    @POST("rest/v1/blocked_apps")
    Call<Map<String, Object>> blockApp(@Body Map<String, Object> blockData);

    @GET("rest/v1/blocked_apps?child_id=eq.{childId}")
    Call<List<Map<String, Object>>> getBlockedApps(@Path("childId") String childId);

    @DELETE("rest/v1/blocked_apps?id=eq.{id}")
    Call<Void> unblockApp(@Path("id") String blockId);

    // ============ MEDIA ============
    @POST("rest/v1/media")
    Call<Map<String, Object>> saveMedia(@Body Map<String, Object> media);

    @GET("rest/v1/media?child_id=eq.{childId}&order=timestamp.desc&limit={limit}")
    Call<List<Map<String, Object>>> getMedia(@Path("childId") String childId, @Path("limit") int limit);

    // ============ ALERTS ============
    @POST("rest/v1/alerts")
    Call<Map<String, Object>> createAlert(@Body Map<String, Object> alert);

    @GET("rest/v1/alerts?child_id=eq.{childId}&is_read=eq.false")
    Call<List<Map<String, Object>>> getUnreadAlerts(@Path("childId") String childId);

    @PATCH("rest/v1/alerts?id=eq.{id}")
    Call<Map<String, Object>> markAlertRead(@Path("id") String alertId);

    // ============ GENERIC QUERY ============
    @Headers("Content-Type: application/json")
    @GET("rest/v1/{table}")
    Call<List<Map<String, Object>>> query(@Path("table") String table, @QueryMap Map<String, String> filters);
}
