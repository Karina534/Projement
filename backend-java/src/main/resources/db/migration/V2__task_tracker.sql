create table projects
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    name varchar(120) not null,
    description varchar(2000),
    owner_id bigint not null references users (id)
);

create table project_members
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null references projects (id) on delete cascade,
    user_id bigint not null references users (id) on delete cascade,
    role varchar(20) not null check (role in ('OWNER', 'MEMBER')),
    constraint uk_project_members_project_user unique (project_id, user_id)
);

create table project_boards
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null unique references projects (id) on delete cascade
);

create table board_columns
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    board_id bigint not null references project_boards (id) on delete cascade,
    type varchar(30) not null check (type in ('BACKLOG', 'TODO', 'IN_PROGRESS', 'REVIEW', 'DONE')),
    title varchar(80) not null,
    position integer not null,
    constraint uk_board_columns_board_type unique (board_id, type)
);

create table tasks
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    title varchar(160) not null,
    description varchar(4000),
    project_id bigint not null references projects (id) on delete cascade,
    column_id bigint not null references board_columns (id),
    assignee_id bigint references users (id),
    deadline date,
    priority varchar(20) not null check (priority in ('LOW', 'MEDIUM', 'HIGH'))
);

create table task_comments
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    task_id bigint not null references tasks (id) on delete cascade,
    author_id bigint not null references users (id),
    content varchar(4000) not null
);

create index idx_project_members_user_id on project_members (user_id);
create index idx_tasks_project_id on tasks (project_id);
create index idx_task_comments_task_id on task_comments (task_id);
