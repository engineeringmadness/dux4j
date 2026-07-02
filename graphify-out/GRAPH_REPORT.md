# Graph Report - .  (2026-07-02)

## Corpus Check
- Corpus is ~6,374 words - fits in a single context window. You may not need a graph.

## Summary
- 358 nodes · 714 edges · 14 communities (13 shown, 1 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 65 edges (avg confidence: 0.79)
- Token cost: 46,339 input · 15,446 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Core Store Implementation|Core Store Implementation]]
- [[_COMMUNITY_Store Builders|Store Builders]]
- [[_COMMUNITY_Documentation & Patterns|Documentation & Patterns]]
- [[_COMMUNITY_Test Utilities|Test Utilities]]
- [[_COMMUNITY_Reflection & Domain|Reflection & Domain]]
- [[_COMMUNITY_CICD & Workflows|CI/CD & Workflows]]
- [[_COMMUNITY_Slice API|Slice API]]
- [[_COMMUNITY_Domain Models|Domain Models]]
- [[_COMMUNITY_Reflection Builder|Reflection Builder]]
- [[_COMMUNITY_Core API Interfaces|Core API Interfaces]]
- [[_COMMUNITY_Async Processing|Async Processing]]
- [[_COMMUNITY_Reflection Components|Reflection Components]]
- [[_COMMUNITY_User Model|User Model]]
- [[_COMMUNITY_Package Root|Package Root]]

## God Nodes (most connected - your core abstractions)
1. `DuxStore` - 44 edges
2. `UserProfile` - 35 edges
3. `Action` - 28 edges
4. `Reducer` - 26 edges
5. `TimeTravel` - 21 edges
6. `State` - 19 edges
7. `Middleware` - 19 edges
8. `DuxStoreBuilder` - 18 edges
9. `DuxSliceBuilder` - 16 edges
10. `DuxStore` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Project README` --references--> `CI Build Workflow`  [EXTRACTED]
  README.md → .github/workflows/ci-build.yml
- `Dux4j` --inspired_by--> `Redux`  [EXTRACTED]
  README.md → agent-docs/Home.md
- `UserProfile` --implements--> `State`  [EXTRACTED]
  README.md → agent-docs/Dux-Store-API.md
- `DuxStore` --implements--> `Time Travel Debugging`  [EXTRACTED]
  agent-docs/Dux-Store-API.md → README.md
- `ReducerBlock` --inherits--> `Reducer`  [EXTRACTED]
  src/main/java/org/flux/store/api/v3/ReducerBlock.java → src/main/java/org/flux/store/api/v1/Reducer.java

## Import Cycles
- None detected.

## Communities (14 total, 1 thin omitted)

### Community 0 - "Core Store Implementation"
Cohesion: 0.08
Nodes (20): Gson, Action, Getter, String, ToString, String, DuxStore, Consumer (+12 more)

### Community 1 - "Store Builders"
Cohesion: 0.09
Nodes (20): Boolean, FunctionalInterface, Reducer, FunctionalInterface, Middleware, DuxStoreBuilder, Consumer, Getter (+12 more)

### Community 2 - "Documentation & Patterns"
Cohesion: 0.09
Nodes (30): Action, Dispatcher, DuxSliceBuilder, DuxStore, Middleware, Reducer, Slice, State (+22 more)

### Community 3 - "Test Utilities"
Cohesion: 0.10
Nodes (16): CsvSource, ParameterizedTest, String, Utilities, BeforeEach, String, Test, StoreTest (+8 more)

### Community 4 - "Reflection & Domain"
Cohesion: 0.09
Nodes (21): Retention, AutoStore, ReducerBlock, AllArgsConstructor, Getter, Override, Setter, String (+13 more)

### Community 5 - "CI/CD & Workflows"
Cohesion: 0.06
Nodes (31): CI Build Workflow, CodeQL Workflow, Maven Publish Workflow, Project README, Action Creators, actions/checkout@v3, actions/checkout@v4, Actions (+23 more)

### Community 6 - "Slice API"
Cohesion: 0.11
Nodes (13): InvalidActionException, Consumer, String, Slice, DuxSlice, Consumer, List, Map (+5 more)

### Community 7 - "Domain Models"
Cohesion: 0.12
Nodes (19): NoArgsConstructor, Author, AllArgsConstructor, Getter, String, Book, AllArgsConstructor, Getter (+11 more)

### Community 8 - "Reflection Builder"
Cohesion: 0.15
Nodes (11): Class, Consumer, Getter, List, Map, String, SuppressWarnings, ReflectionDuxSliceBuilder (+3 more)

### Community 9 - "Core API Interfaces"
Cohesion: 0.12
Nodes (9): Cloneable, Diffable, DiffResult, Serializable, Override, State, Consumer, Store (+1 more)

### Community 10 - "Async Processing"
Cohesion: 0.13
Nodes (11): CompletableFuture, Exception, AsyncNotificationException, String, Throwable, InvalidActionException, String, Throwable (+3 more)

### Community 11 - "Reflection Components"
Cohesion: 0.43
Nodes (7): SetEmailReducer, SetNameReducer, Action, Slice, AutoStore, ReducerBlock, ReflectionDuxSliceBuilder

### Community 12 - "User Model"
Cohesion: 0.47
Nodes (5): Getter, Integer, Setter, String, User

## Knowledge Gaps
- **36 isolated node(s):** `dev.engineeringmadness:dux4j`, `Java CI with Maven`, `CodeQL Analysis`, `Maven Package`, `Unidirectional State Management` (+31 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DuxStore` connect `Core Store Implementation` to `Store Builders`, `Test Utilities`, `Reflection & Domain`, `Slice API`, `Domain Models`, `Core API Interfaces`?**
  _High betweenness centrality (0.243) - this node is a cross-community bridge._
- **Why does `DuxStore` connect `Documentation & Patterns` to `Async Processing`, `CI/CD & Workflows`?**
  _High betweenness centrality (0.174) - this node is a cross-community bridge._
- **Why does `UserProfile` connect `Reflection & Domain` to `Store Builders`, `Test Utilities`, `Slice API`, `Reflection Builder`, `Core API Interfaces`?**
  _High betweenness centrality (0.160) - this node is a cross-community bridge._
- **What connects `dev.engineeringmadness:dux4j`, `Java CI with Maven`, `CodeQL Analysis` to the rest of the system?**
  _36 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Core Store Implementation` be split into smaller, more focused modules?**
  _Cohesion score 0.08295625942684766 - nodes in this community are weakly interconnected._
- **Should `Store Builders` be split into smaller, more focused modules?**
  _Cohesion score 0.08961593172119488 - nodes in this community are weakly interconnected._
- **Should `Documentation & Patterns` be split into smaller, more focused modules?**
  _Cohesion score 0.0907563025210084 - nodes in this community are weakly interconnected._