-- Add CV fields to pfe_applications table
ALTER TABLE pfe_applications 
ADD COLUMN cv_file_name VARCHAR(255),
ADD COLUMN cv_file_url VARCHAR(500),
ADD COLUMN cv_original_file_name VARCHAR(255);

-- Add comment to describe the columns
COMMENT ON COLUMN pfe_applications.cv_file_name IS 'Unique filename for the CV stored on server';
COMMENT ON COLUMN pfe_applications.cv_file_url IS 'URL path to access the CV file';
COMMENT ON COLUMN pfe_applications.cv_original_file_name IS 'Original filename of the uploaded CV';
