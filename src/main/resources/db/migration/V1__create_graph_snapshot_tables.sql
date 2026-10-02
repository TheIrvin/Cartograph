CREATE TABLE IF NOT EXISTS repository_snapshot (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repository TEXT NOT NULL,
    commit_sha TEXT NOT NULL,
    indexed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metrics_json TEXT NOT NULL,
    warnings_json TEXT NOT NULL,
    CONSTRAINT uq_repository_commit UNIQUE (repository, commit_sha)
);

CREATE TABLE IF NOT EXISTS graph_node (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    snapshot_id INTEGER NOT NULL,
    stable_id TEXT NOT NULL,
    kind TEXT NOT NULL,
    name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    start_line INTEGER NOT NULL,
    end_line INTEGER NOT NULL,
    start_column INTEGER NOT NULL,
    end_column INTEGER NOT NULL,
    CONSTRAINT uq_snapshot_node UNIQUE (snapshot_id, stable_id),
    CONSTRAINT fk_node_snapshot FOREIGN KEY (snapshot_id) REFERENCES repository_snapshot(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS graph_edge (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    snapshot_id INTEGER NOT NULL,
    from_id TEXT NOT NULL,
    to_id TEXT NOT NULL,
    kind TEXT NOT NULL,
    confidence REAL NOT NULL,
    file_path TEXT,
    start_line INTEGER,
    start_column INTEGER,
    end_line INTEGER,
    end_column INTEGER,
    CONSTRAINT uq_snapshot_edge UNIQUE (snapshot_id, from_id, to_id, kind, file_path, start_line, start_column, end_line, end_column),
    CONSTRAINT fk_edge_snapshot FOREIGN KEY (snapshot_id) REFERENCES repository_snapshot(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_snapshot_lookup ON repository_snapshot(repository, commit_sha);
CREATE INDEX IF NOT EXISTS idx_edge_from ON graph_edge(snapshot_id, from_id);
CREATE INDEX IF NOT EXISTS idx_edge_to ON graph_edge(snapshot_id, to_id);
