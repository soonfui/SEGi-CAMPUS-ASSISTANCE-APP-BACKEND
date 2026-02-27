-- Marketplace Items 表创建脚本
-- 注意：condition 是 MySQL 保留关键字，需要用反引号括起来

CREATE TABLE IF NOT EXISTS marketplace_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  seller_id INT NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  price DECIMAL(10,2),
  item_condition ENUM('New','Like New','Good','Fair','Poor') DEFAULT 'Good',
  description TEXT,
  location VARCHAR(200),
  image_url VARCHAR(255),
  contact_email VARCHAR(100),
  status ENUM('For Sale','Sold') DEFAULT 'For Sale',
  views INT DEFAULT 0,
  is_active BOOLEAN DEFAULT 1,
  date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (seller_id) REFERENCES users(user_id)
);

