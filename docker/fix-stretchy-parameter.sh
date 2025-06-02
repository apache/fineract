#!/bin/bash
# Script to fix the stretchy_parameter table issue
set -e

echo "Checking if stretchy_parameter table exists..."

DB_CHECK=$(PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -d fineract_default -t -c "\dt public.stretchy_parameter" 2>/dev/null | grep -c stretchy_parameter || true)

if [ "$DB_CHECK" -eq "0" ]; then
    echo "Creating stretchy_parameter table manually..."
    
    PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -d fineract_default -c "
    CREATE TABLE IF NOT EXISTS public.stretchy_parameter (
      id SERIAL PRIMARY KEY,
      parameter_name VARCHAR(45) NOT NULL UNIQUE,
      parameter_variable VARCHAR(45),
      parameter_label VARCHAR(45) NOT NULL,
      parameter_displayType VARCHAR(45) NOT NULL,
      parameter_FormatType VARCHAR(10) NOT NULL,
      parameter_default VARCHAR(45) NOT NULL,
      special VARCHAR(1),
      selectOne VARCHAR(1),
      selectAll VARCHAR(1),
      parameter_sql TEXT,
      parent_id INT,
      CONSTRAINT fk_stretchy_parameter_001 FOREIGN KEY (parent_id) REFERENCES public.stretchy_parameter (id)
    );
    "
    echo "stretchy_parameter table created successfully."
else
    echo "stretchy_parameter table already exists."
fi
