-- traffic_cache needs a unique key so the event ingest path can upsert
-- (INSERT ... ON DUPLICATE KEY UPDATE) instead of per-row DELETE + INSERT.
-- Deduplicate existing rows first: keep the newest id per (sdk_key, toggle_key, type).
delete t1 from traffic_cache t1
    inner join traffic_cache t2
        on t1.sdk_key = t2.sdk_key
            and t1.toggle_key = t2.toggle_key
            and t1.type = t2.type
            and t1.id < t2.id;

alter table traffic_cache
    add unique key uk_sdk_toggle_type (sdk_key, toggle_key, type);
