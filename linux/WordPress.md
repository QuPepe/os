# 🚀 Practical Project: Understanding OS Services in Action: Deploying a Local WordPress Server on Ubuntu

## 🌐 Objectives
In this practical project, you will manually deploy a WordPress website on Ubuntu using the LAMP stack (Linux, Apache, MySQL, PHP). This exercise focuses on how operating systems manage services, processes, users, and file permissions in a real-world web application environment.

By the end of this project, you will understand:
- How Linux manages system services with systemctl
- Proper use of file ownership and permissions
- Interactions between web, database, and scripting services
- The role of configuration files, modules, and network ports

## 🌐 Step-by-Step Setup Instructions
### ⚙️ Update Your System
- `sudo apt update && sudo apt upgrade`

### ⚙️ Install Apache Web Server
- `sudo apt install apache2`
- `sudo systemctl enable apache2` # Optional
- `sudo systemctl start apache2`

### ⚙️ Configure Apache Web Server
- `sudo nano /etc/apache2/apache2.conf` and add: `ServerName localhost` # Optional: Backup the file first
- `sudo nano /etc/apache2/sites-available/000-default.conf` and change `DocumentRoot` to `/var/www/wordpress` # Optional: Backup the file first
- `sudo mkdir /var/www/wordpress`
- Restart Apache: `sudo systemctl restart apache2`
- Open a browser and visit: `http://localhost`

### ⚙️ Install MySQL Server
- `sudo apt install mysql-server`
- `sudo systemctl enable mysql` # Optional
- `sudo systemctl start mysql`

### ⚙️ Create a MySQL Database and a User for WordPress
- Log into MySQL:`sudo mysql`
- Inside the MySQL prompt, run:
- `CREATE USER 'wpadmin'@'localhost' IDENTIFIED WITH mysql_native_password BY '<your-password>';`
- `CREATE DATABASE wordpress_db;`
- `GRANT ALL PRIVILEGES ON wordpress_db.* TO 'wpadmin'@'localhost';`
- `FLUSH PRIVILEGES;`
- `EXIT;`

### ⚙️ Install PHP and Required Extensions
- `sudo apt install php8.3 php8.3-mysql libapache2-mod-php8.3 php8.3-cli php8.3-curl php8.3-gd php8.3-mbstring php8.3-xml php8.3-soap php8.3-intl php8.3-zip unzip`
- `apache2ctl -M | grep php` # Show loaded Apache modules and check if there is "php_module (shared)"
- Restart Apache: `sudo systemctl restart apache2`

### ⚙️ Download and Extract WordPress
- `cd /tmp`
- `wget https://wordpress.org/latest.tar.gz`
- `tar -xvzf latest.tar.gz`

### ⚙️ Copy WordPress Files
- `sudo cp -r wordpress/. /var/www/wordpress/` # "." means all files including hidden files

### ⚙️ Set Permissions and Ownership
- `sudo chmod 755 /var/www/wordpress`
- `sudo chown -R www-data:www-data /var/www/wordpress`

### ⚙️ Configure WordPress
- `cd /var/www/wordpress`
- `sudo cp wp-config-sample.php wp-config.php`
- `sudo chown www-data:www-data wp-config.php`
- `sudo nano wp-config.php` and change:
- `DB_NAME`: `wordpress_db`
- `DB_USER`: `wpadmin`
- `DB_PASSWORD`: `<your-password>`

### ⚙️ Test WordPress
- Open a browser and visit: `http://localhost`
- Admin page: `http://localhost/wp-admin`

### ⚙️ Play with WordPress
- Create and publish posts
- Change the site theme

## 🌐 Deep Dive
### ⚙️ Setup phpMyAdmin
- `sudo apt install phpmyadmin`
- `sudo nano /etc/apache2/apache2.conf` and add this line at the bottom: `Include /etc/phpmyadmin/apache.conf`
- `sudo systemctl restart apache2`
- Open a browser and visit: `http://localhost/phpmyadmin` (Loign: wpadmin)

### ⚙️ Apache2 Commands
- `sudo systemctl start apache2` # Start Apache service
- `sudo systemctl status apache2` # Check Apache's status
- `sudo systemctl restart apache2` # Restart Apache service
- `tail -n 50 /var/log/apache2/access.log` # View access log
- `tail -f /var/log/apache2/access.log` # View access log
- `tail -n 50 /var/log/apache2/error.log` # View error log

### ⚙️ Inspect Apache-related Processes and Threads
- `ps -ef | grep apache` # Lists all processes (-e for all, -f for full format)
- `ps -eLf | grep apache` # Lists all threads of all processes (L = LWP = Light Weight Processes)

### ⚙️ Backup and Restore
- Try this shell script: https://github.com/FrankWorldview/os/blob/main/code/backup.sh

### ⚙️ PHP 8.3 Packages and Their Purposes
| Package Name            | Purpose / Description                                     | Required for WordPress? |
|-------------------------|-----------------------------------------------------------|-------------------------|
| `php8.3`                | Core PHP interpreter                                      | ✅ Yes                  |
| `php8.3-mysql`          | MySQL database integration (`mysqli`, `PDO`)              | ✅ Yes                  |
| `libapache2-mod-php8.3` | PHP module for Apache2 web server                         | ✅ Yes                  |
| `php8.3-cli`            | Command-line PHP interface                                | ✅ Highly recommended   |
| `php8.3-curl`           | HTTP requests from PHP (`curl_exec`, `wp_remote_get`)     | ✅ Yes                  |
| `php8.3-gd`             | Image processing (thumbnails, resizing, etc.)             | ✅ Yes                  |
| `php8.3-mbstring`       | Multibyte string handling (UTF-8, non-English content)    | ✅ Yes                  |
| `php8.3-xml`            | XML parsing and DOM support (feeds, plugins, etc.)        | ✅ Yes                  |
| `php8.3-soap`           | SOAP web service support                                  | ⚠️ Optional             |
| `php8.3-intl`           | Internationalization (locale, date/time, transliteration) | ⚠️ Optional             |
| `php8.3-zip`            | Zip file support (plugin/theme uploads)                   | ✅ Yes                  |
| `unzip`                 | Command-line utility to extract `.zip` files              | ✅ Yes                  |

## 🌐 References
https://developer.wordpress.org/advanced-administration/before-install/howto-install/
