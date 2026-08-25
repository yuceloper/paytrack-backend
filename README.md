# PayTrack Backend

PayTrack, kredi kartları, krediler, abonelikler, faturalar ve diğer düzenli ödemeleri tek merkezden takip etmeyi amaçlayan kişisel finans takip uygulamasıdır.

Bu repository, PayTrack'in Spring Boot tabanlı backend servisidir.

## Proje Amacı

Kullanıcının yaklaşan tüm finansal yükümlülüklerini tek yerde görebilmesini, ödeme tarihlerini kaçırmamasını ve aylık nakit çıkışını önceden planlayabilmesini sağlamak.

PayTrack yalnızca bir gider listesi değil; kullanıcının "önümüzdeki 7 gün ne kadar ödeme var?", "maaşa kadar ne kadar para ayırmalıyım?" ve "aboneliklerim yılda bana ne kadar maliyet çıkarıyor?" gibi sorulara hızlı cevap verebildiği bir kişisel ödeme merkezi olmayı hedefler.

## Kapsam

İlk sürümde desteklenecek ana varlıklar:

- Kredi kartları
- Krediler
- Abonelikler
- Faturalar
- Diğer düzenli ödemeler
- Tek seferlik ödemeler

## Temel Özellikler

- Kullanıcı bazlı ödeme takibi
- Tekrarlayan ödeme planları
- Yaklaşan ödemeler
- Geciken ödemeler
- Ödendi / bekliyor durum yönetimi
- Aylık ve haftalık toplam ödeme hesapları
- Kredi taksit takibi
- Kredi kartı hesap kesim / son ödeme tarihi takibi
- Abonelik yenileme takibi
- Bildirim planlarının backend tarafında yönetimi
- Dashboard özet API'leri

## Teknoloji

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Bean Validation
- Lombok

## Hedef Domain Modeli

```text
User
 ├── CreditCard
 ├── Loan
 ├── Subscription
 ├── Bill
 └── OtherPaymentSource

PaymentSource
 └── PaymentSchedule
      └── PaymentOccurrence
```

İlk geliştirme aşamasında model sade tutulabilir; domain ihtiyaçları netleştikçe `PaymentSource`, `PaymentSchedule` ve gerçekleşmiş ödeme kayıtları ayrıştırılacaktır.

## Planlanan API Grupları

```text
/api/v1/auth
/api/v1/users
/api/v1/credit-cards
/api/v1/loans
/api/v1/subscriptions
/api/v1/bills
/api/v1/payments
/api/v1/dashboard
/api/v1/notifications
```

## Örnek Dashboard Verileri

Backend aşağıdaki gibi özet bilgileri sağlayacaktır:

- Bu ay ödenecek toplam tutar
- Önümüzdeki 7 gün içindeki ödemeler
- Bugün ödenecekler
- Geciken ödemeler
- Toplam kredi kartı borcu
- Aylık abonelik maliyeti
- Yıllık abonelik maliyeti
- Maaşa kadar ayrılması gereken tahmini tutar

## Yol Haritası

Detaylı plan için [ROADMAP.md](ROADMAP.md) dosyasına bakın.

## Yapılacaklar

Aktif geliştirme listesi için [TODO.md](TODO.md) dosyasına bakın.

## Mobile App

Flutter istemcisi ayrı repository'de tutulur:

`yuceloper/paytrack-app`

## Durum

Proje erken geliştirme aşamasındadır. İlk hedef gerçek backend verisiyle çalışan basit fakat kullanılabilir bir MVP çıkarmaktır.
