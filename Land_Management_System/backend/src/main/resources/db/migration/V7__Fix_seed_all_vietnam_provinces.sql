INSERT INTO administrative_unit (name, unit_level, unit_kind, parent_id, is_active)
SELECT 
    v.name,
    'PROVINCE',
      NULL,
    c.id,
    true
FROM (
    SELECT 'Thành phố Hà Nội' AS name
    UNION ALL SELECT 'Thành phố Huế'
    UNION ALL SELECT 'Tỉnh Lai Châu'
    UNION ALL SELECT 'Tỉnh Điện Biên'
    UNION ALL SELECT 'Tỉnh Sơn La'
    UNION ALL SELECT 'Tỉnh Lạng Sơn'
    UNION ALL SELECT 'Tỉnh Quảng Ninh'
    UNION ALL SELECT 'Tỉnh Thanh Hóa'
    UNION ALL SELECT 'Tỉnh Nghệ An'
    UNION ALL SELECT 'Tỉnh Hà Tĩnh'
    UNION ALL SELECT 'Tỉnh Cao Bằng'
    UNION ALL SELECT 'Tỉnh Tuyên Quang'
    UNION ALL SELECT 'Tỉnh Lào Cai'
    UNION ALL SELECT 'Tỉnh Thái Nguyên'
    UNION ALL SELECT 'Tỉnh Phú Thọ'
    UNION ALL SELECT 'Tỉnh Bắc Ninh'
    UNION ALL SELECT 'Tỉnh Hưng Yên'
    UNION ALL SELECT 'Thành phố Hải Phòng'
    UNION ALL SELECT 'Tỉnh Ninh Bình'
    UNION ALL SELECT 'Tỉnh Quảng Trị'
    UNION ALL SELECT 'Thành phố Đà Nẵng'
    UNION ALL SELECT 'Tỉnh Quảng Ngãi'
    UNION ALL SELECT 'Tỉnh Gia Lai'
    UNION ALL SELECT 'Tỉnh Khánh Hòa'
    UNION ALL SELECT 'Tỉnh Lâm Đồng'
    UNION ALL SELECT 'Tỉnh Đắk Lắk'
    UNION ALL SELECT 'Thành phố Hồ Chí Minh'
    UNION ALL SELECT 'Tỉnh Đồng Nai'
    UNION ALL SELECT 'Tỉnh Tây Ninh'
    UNION ALL SELECT 'Thành phố Cần Thơ'
    UNION ALL SELECT 'Tỉnh Vĩnh Long'
    UNION ALL SELECT 'Tỉnh Đồng Tháp'
    UNION ALL SELECT 'Tỉnh Cà Mau'
    UNION ALL SELECT 'Tỉnh An Giang'
) v
JOIN administrative_unit c
  ON c.unit_level = 'CENTRAL'
 AND c.is_active = true
WHERE NOT EXISTS (
    SELECT 1
    FROM administrative_unit au
    WHERE au.name = v.name
      AND au.unit_level = 'PROVINCE'
);