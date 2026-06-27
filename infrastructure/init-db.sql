-- Database initialization script for Cloud Backup System
-- This script creates the necessary database objects

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create basic schema
CREATE SCHEMA IF NOT EXISTS public;

-- Grant permissions
GRANT USAGE ON SCHEMA public TO cloudbackup;
GRANT CREATE ON SCHEMA public TO cloudbackup;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cloudbackup;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cloudbackup;
