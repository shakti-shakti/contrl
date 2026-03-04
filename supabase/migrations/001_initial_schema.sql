-- Supabase PostgreSQL Schema for Transparent Parental Control App
-- Create this directly in your Supabase SQL editor

-- Profiles table (all users - parents and children)
CREATE TABLE profiles (
  id UUID REFERENCES auth.users PRIMARY KEY,
  device_mode TEXT CHECK (device_mode IN ('parent', 'child')) NOT NULL,
  device_name TEXT NOT NULL,
  master_pin TEXT, -- Only for parent accounts
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Parent-Child relationships
CREATE TABLE relationships (
  id SERIAL PRIMARY KEY,
  parent_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  child_name TEXT,
  child_age INTEGER,
  paired_at TIMESTAMP DEFAULT NOW(),
  is_active BOOLEAN DEFAULT TRUE,
  CONSTRAINT parent_not_child CHECK (parent_id != child_id)
);

-- Location tracking (transparent - child knows they're tracked)
CREATE TABLE locations (
  id SERIAL PRIMARY KEY,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  accuracy FLOAT,
  battery_level INTEGER,
  address TEXT,
  timestamp TIMESTAMP DEFAULT NOW()
);

-- App usage statistics
CREATE TABLE app_usage (
  id SERIAL PRIMARY KEY,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  package_name TEXT,
  app_name TEXT,
  usage_duration INTEGER, -- in seconds
  category TEXT, -- 'game', 'social', 'education', etc.
  timestamp TIMESTAMP DEFAULT NOW()
);

-- Geofences (safe zones)
CREATE TABLE geofences (
  id SERIAL PRIMARY KEY,
  parent_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  radius FLOAT NOT NULL, -- in meters
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Notifications mirror (shows what child is being notified about)
CREATE TABLE notifications_log (
  id SERIAL PRIMARY KEY,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  app_package TEXT,
  title TEXT,
  text TEXT,
  sent_at TIMESTAMP DEFAULT NOW()
);

-- Media references (photos, videos, screenshots)
CREATE TABLE media (
  id SERIAL PRIMARY KEY,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  media_type TEXT CHECK (media_type IN ('photo', 'video', 'screenshot')),
  storage_path TEXT, -- Supabase storage path
  timestamp TIMESTAMP DEFAULT NOW()
);

-- Blocked apps list per child
CREATE TABLE blocked_apps (
  id SERIAL PRIMARY KEY,
  parent_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  package_name TEXT NOT NULL,
  app_name TEXT,
  blocked_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(child_id, package_name)
);

-- App schedules (when apps are blocked)
CREATE TABLE app_schedules (
  id SERIAL PRIMARY KEY,
  parent_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  app_package TEXT,
  start_time TIME,
  end_time TIME,
  days TEXT, -- comma-separated: mon,tue,wed,thu,fri,sat,sun
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Alerts (geofence, app blocks, etc.)
CREATE TABLE alerts (
  id SERIAL PRIMARY KEY,
  child_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  alert_type TEXT, -- 'geofence_enter', 'geofence_exit', 'app_blocked', etc.
  message TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Row Level Security (RLS) Policies
-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE relationships ENABLE ROW LEVEL SECURITY;
ALTER TABLE locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE geofences ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE media ENABLE ROW LEVEL SECURITY;
ALTER TABLE blocked_apps ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE alerts ENABLE ROW LEVEL SECURITY;

-- Indexes for better performance
CREATE INDEX idx_locations_child_timestamp ON locations(child_id, timestamp DESC);
CREATE INDEX idx_app_usage_child_timestamp ON app_usage(child_id, timestamp DESC);
CREATE INDEX idx_relationships_parent ON relationships(parent_id);
CREATE INDEX idx_relationships_child ON relationships(child_id);
CREATE INDEX idx_geofences_child ON geofences(child_id);
CREATE INDEX idx_blocked_apps_child ON blocked_apps(child_id);
CREATE INDEX idx_alerts_child ON alerts(child_id);
