CREATE TABLE task.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    task_name VARCHAR(255) NOT NULL,

    description TEXT,

    status VARCHAR(30) NOT NULL,

    priority_level VARCHAR(30) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE,

    start_time TIME,

    end_time TIME,

    day_of_week VARCHAR(20),

    note TEXT,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE,

    deleted_at TIMESTAMP WITH TIME ZONE
);