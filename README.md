# Quy tắc tạo Branch và mục đích từng Branch

| Branch | Mục đích | Ví dụ tên Branch |
|---------------|---------------|---------------|
| `main`  | Chứa code ổn định, đã qua kiểm thử, chỉ cập nhật khi release.  | `main`  |
| `dev`  | Tích hợp code từ các feature branch, chuẩn bị cho release.  | `dev`  |
| `realease-*`  | Chuẩn bị cho bản phát hành (fix bug, update docs).  | `release-v1.0`  |
| `feature/*`  | Phát triển tính năng mới. Mỗi feature có branch riêng.  | `feature/login`  |
| `hotfix/*`  | Sửa lỗi khẩn cấp trên production (main).  | `hotfix/auth-bug`  |
| `bugfix/*`  | Sửa lỗi trên branch dev. | `bugfix/404-error`  |

## Quy trình làm việc
- Từ `main`
  - Tạo branch `dev` làm nơi tích hợp chính
  - Khi release, tạo release-* từ dev → Merge vào main và dev sau khi hoàn thành.
- Phát triển tính năng
  - Tạo branch `feature/*` từ `dev`.
  - Khi xong, merge vào `dev` qua **Pull Request (PR)**.
- Sửa lỗi
  - Lỗi trên production: Tạo `hotfix/*` từ `main` → Merge vào `main` và `dev`.
  - Lỗi khi đang phát triển: Tạo `bugfix/*` từ `dev`.
## Lưu ý quan trọng
📌 `main` luôn **deployable**: Chỉ merge code đã tested.<br>
📌 Mỗi task có 1 branch riêng: Tránh nhảy branch tùy tiện.<br>
📌 Review code trước khi merge: Dùng Pull Request + assign người review.<br>
📌 Xóa branch sau khi merge ( trừ `main`, `dev` )
