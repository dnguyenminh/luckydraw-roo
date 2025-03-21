# Hệ Thống Phân Chia Và Bốc Thăm Quà Tặng

## 1. Giới thiệu
Hệ thống này giúp phân chia quà tặng vào các gói sao cho tỉ lệ phần trăm của từng loại quà vẫn giữ nguyên. Sau khi phân chia, người dùng có thể bốc thăm ngẫu nhiên để nhận quà từ các gói.

## 2. Chức năng chính
### 2.1. Phân chia quà tặng
- Chia tổng số lượng quà (nhiều loại) vào **V gói**.
- Đảm bảo tỉ lệ quà trong từng gói giống như tổng ban đầu.
- Hỗ trợ nhiều loại quà khác nhau.
- Có thể điều chỉnh tỷ lệ trúng từng loại quà.
- Đảm bảo số lượng tối thiểu mỗi loại quà trong từng gói (nếu cần).

### 2.2. Bốc thăm quà ngẫu nhiên
- Bốc ngẫu nhiên một phần quà từ các gói.
- Khi bốc được một phần quà, số lượng quà đó trong gói giảm đi.
- Nếu quà đã hết, sẽ không thể bốc quà đó nữa.
- Nếu tất cả các gói đều hết quà, hệ thống sẽ thông báo.

### 2.3. Lưu kết quả
- Xuất danh sách gói quà ra file CSV.
- Lưu kết quả bốc thăm vào file hoặc cơ sở dữ liệu (nếu mở rộng).

## 3. Cách sử dụng
1. **Chạy chương trình**, hệ thống sẽ tự động phân chia quà vào các gói.
2. **Kiểm tra danh sách quà tặng** trước khi bốc thăm.
3. **Nhập số lần bốc thăm**, hệ thống sẽ chọn quà ngẫu nhiên từ các gói.
4. **Xem lại danh sách gói quà** sau khi bốc thăm để kiểm tra số lượng quà còn lại.
5. **Xuất kết quả ra file CSV** để lưu trữ hoặc kiểm tra.

## 4. Ví dụ Kết Quả
### Trước khi bốc thăm:
```plaintext
Pack 1: {Gift1=4, Gift2=2, Gift3=1, Empty=3}
Pack 2: {Gift1=4, Gift2=2, Gift3=1, Empty=2}
Pack 3: {Gift1=4, Gift2=3, Gift3=2, Empty=1}
Pack 4: {Gift1=3, Gift2=2, Gift3=2, Empty=4}
```

### Sau khi bốc thăm:
```plaintext
Lần 1: Bạn nhận được Gift1
Lần 2: Bạn nhận được Gift3
Lần 3: Bạn nhận được Empty
Lần 4: Bạn nhận được Gift2
Lần 5: Bạn nhận được Gift1
```

### Sau khi bốc thăm:
```plaintext
Pack 1: {Gift1=3, Gift2=2, Gift3=1, Empty=3}
Pack 2: {Gift1=4, Gift2=2, Gift3=1, Empty=2}
Pack 3: {Gift1=3, Gift2=3, Gift3=1, Empty=1}
Pack 4: {Gift1=3, Gift2=1, Gift3=2, Empty=4}
```

## 5. Giải pháp cho hệ thống web server với nhiều node
- **Sử dụng cơ sở dữ liệu tập trung** để lưu trạng thái của các gói quà, đảm bảo tính nhất quán khi có nhiều node truy cập.
- **Dùng hàng đợi tin nhắn (message queue)** như Kafka hoặc RabbitMQ để xử lý yêu cầu bốc thăm theo thứ tự.
- **Triển khai cơ chế lock hoặc distributed transaction** để tránh tình trạng nhiều người dùng nhận cùng một món quà.
- **Sử dụng cache phân tán** như Redis để tăng tốc truy xuất dữ liệu và giảm tải cho cơ sở dữ liệu.
- **Đồng bộ hóa dữ liệu** giữa các node thông qua hệ thống replication hoặc event-driven architecture.

## 6. Mở rộng
- **Thêm giao diện đồ họa** để người dùng bốc thăm dễ dàng hơn.
- **Lưu dữ liệu vào cơ sở dữ liệu** để quản lý nhiều chương trình rút thăm.
- **Thêm tính năng ưu tiên một số loại quà** khi cần.

---

**Tác giả:** Nguyễn Minh Đức

**Ngày cập nhật:** Feb 05th, 2025
