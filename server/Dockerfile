# Reproducible build: offline cargo build against vendored dependencies
# (vendor-deps/), glibc-linked because ring/rustls require a C toolchain and
# the base image ships gcc. The runtime uses the same base so offline builds do\n# not need to resolve or update a second distribution image.
# (bullseye => 2.31); running the binary on older images (e.g. buster-slim,
# glibc 2.28) fails with "GLIBC_2.29 not found".
#
# If musl-tools cannot be installed in your environment (restricted network),
# the binary also runs unchanged on the rust:1.67.0 base image.

FROM rust:1.67.0 AS build

WORKDIR /app
COPY . /app
WORKDIR /app/server

# Build fully offline against vendored dependencies (see vendor-deps/ and
# .cargo/config.toml, which is committed with the vendored-sources redirect)

# Drop .cargo_vcs_info.json entries (Windows-vendored trees fail checksum on Linux)
RUN find vendor-deps -name .cargo-checksum.json -exec sed -i 's/".cargo_vcs_info.json":"[a-f0-9]*",//g; s/,".cargo_vcs_info.json":"[a-f0-9]*"//g' {} \; && find vendor-deps -name .cargo_vcs_info.json -delete

RUN rustc -V
RUN cargo build --release --offline --verbose --bin yl-toggle-server --bin healthcheck

FROM rust:1.67.0
COPY --from=build /app/server/target/release/yl-toggle-server ./yl-toggle-server
COPY --from=build /app/server/target/release/healthcheck ./healthcheck

CMD [ "./yl-toggle-server" ]




