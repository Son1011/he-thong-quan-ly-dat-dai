## 05 — Diagrams (Mermaid)

### 5.1 ERD (Entity Relationship Diagram)

```mermaid
erDiagram
  ADMINISTRATIVEUNIT ||--o{ ADMINISTRATIVEUNIT : parent_of
  ADMINISTRATIVEUNIT ||--o{ USER : has_users
  ADMINISTRATIVEUNIT ||--o{ DOSSIER : origin_of
  ADMINISTRATIVEUNIT ||--o{ DOSSIER : assigned_to
  USER ||--o{ DOSSIER : created_by
  DOSSIER ||--o{ APPROVALHISTORY : has_history
  USER ||--o{ APPROVALHISTORY : acts
  DOSSIER ||--o{ DOSSIERATTACHMENT : has_attachments
  USER ||--o{ DOSSIERATTACHMENT : uploads
```

---

### 5.2 State machine — trạng thái hồ sơ

```mermaid
stateDiagram-v2
  [*] --> PENDING : Xã tạo hồ sơ\n(assign=Tỉnh)

  PENDING --> APPROVED : Tỉnh approve\n(assign=Xã)
  PENDING --> RETURNED : Tỉnh return (note bắt buộc)\n(assign=Xã)
  PENDING --> ESCALATED : Tỉnh escalate\n(assign=TW)

  ESCALATED --> APPROVED : TW approve\n(assign=origin_unit)
  ESCALATED --> RETURNED : TW return\n(assign=origin_unit)

  RETURNED --> PENDING : Xã submit\n(assign=Tỉnh)

  note right of ESCALATED
    Hồ sơ tỉnh tạo:\n tạo mới => ESCALATED (assign=TW)
  end note
```

---

### 5.3 Sequence — login + load profile + route guard

```mermaid
sequenceDiagram
  autonumber
  participant U as User (Browser)
  participant FE as Frontend (React)
  participant BE as Backend (Spring Boot)

  U->>FE: Nhập username/password
  FE->>BE: POST /auth/login
  BE-->>FE: {access_token}
  FE->>FE: localStorage.setItem(access_token)

  FE->>BE: GET /me (Bearer)
  BE-->>FE: Me {role, unit_id, must_change_password}
  alt must_change_password == true
    FE-->>U: Redirect /change-password
  else
    FE-->>U: Render AppShell + menu theo role
  end
```

---

### 5.4 Sequence — Xã tạo hồ sơ → Tỉnh duyệt (kèm chữ ký)

```mermaid
sequenceDiagram
  autonumber
  participant C as Cán bộ Xã (FE)
  participant P as Cán bộ Tỉnh (FE)
  participant BE as Backend
  participant DB as MySQL

  C->>BE: POST /dossiers {title}
  BE->>DB: INSERT dossier(PENDING, origin=COMMUNE, assigned_to=PROVINCE)
  BE->>DB: INSERT approvalhistory(action=create)
  BE-->>C: DossierRead

  P->>BE: GET /dossiers/inbox
  BE->>DB: SELECT dossiers WHERE assigned_to=province
  BE-->>P: danh sách inbox

  P->>P: Ký trên canvas => base64 PNG
  P->>BE: POST /dossiers/{id}/actions {approve, note?, signature_base64_png}
  BE->>DB: UPDATE dossier(APPROVED, assigned_to=origin_unit)
  BE->>DB: INSERT approvalhistory(action=approve, signature=...)
  BE-->>P: DossierRead (updated)
```

---

### 5.5 Sequence — Upload & download tài liệu đính kèm

```mermaid
sequenceDiagram
  autonumber
  participant FE as Frontend
  participant BE as Backend
  participant FS as Uploads Folder
  participant DB as MySQL

  FE->>BE: POST /dossiers/{id}/attachments (multipart)
  BE->>FS: Save file as <uuid>.<ext>
  BE->>DB: INSERT dossierattachment(storage_path, original_filename, ...)
  BE-->>FE: AttachmentRead

  FE->>BE: GET /attachments/{attachment_id}/download
  BE->>FS: Read file
  BE-->>FE: FileResponse (blob)
```

---

### 5.6 Flowchart — UI hiển thị theo role (menu + action buttons)

```mermaid
flowchart TD
  A[Load /me] --> B{me.role}
  B -->|ADMIN| C[Menu: Admin]
  B -->|CENTRAL_OFFICER| D[Menu: Stats, Units, Inbox, Dossiers]
  B -->|PROVINCE_OFFICER| E[Menu: Inbox, Dossiers, New dossier, Sent-to-central, Stats, Units]
  B -->|COMMUNE_OFFICER| F[Menu: Inbox, Dossiers, New dossier]

  G[DossierDetail] --> H{assigned_to_unit_id == me.unit_id?}
  H -->|No| I[Ẩn action buttons]
  H -->|Yes| J{role}
  J -->|PROVINCE_OFFICER| K[Actions: approve/return/escalate]
  J -->|CENTRAL_OFFICER| L[Actions: approve/return]
  J -->|COMMUNE_OFFICER| M[Actions: submit]
```
