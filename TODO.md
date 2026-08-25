# PayTrack Backend TODO

Aktif geliştirme sırasında tamamlanacak işler.

## Şimdi

- [x] PostgreSQL local development yapılandırması
- [x] `docker-compose.yml`
- [x] `application.yml` ve local profile
- [x] User entity
- [x] Payment domain modelini modüler source referanslarıyla netleştir
- [x] CreditCard entity
- [x] Loan entity
- [x] Subscription entity
- [x] Bill entity
- [x] Repository port + persistence adapter katmanı
- [x] Service katmanı
- [x] DTO / mapper yapısı
- [x] CreditCard CRUD API
- [x] Loan CRUD API
- [x] Subscription CRUD API
- [x] Bill CRUD API
- [x] `GET /api/v1/payments/upcoming`
- [x] `GET /api/v1/dashboard/summary`

## Sonraki

- [ ] Payment create/update/delete API
- [ ] Tekrarlayan ödeme oluşturma
- [ ] Kredi taksit planı üretme
- [ ] Ödeme tamamlandı işaretleme
- [x] Geciken ödeme sorgulama altyapısı
- [ ] Validation mesajlarını standardize et
- [x] Global exception handler altyapısı
- [ ] Swagger / OpenAPI

## Bildirimler

- [ ] Kullanıcı bildirim tercihleri
- [ ] Son ödeme tarihinden önce reminder hesaplama
- [ ] Push token saklama
- [ ] Bildirim scheduler
- [ ] Bildirim geçmişi

## Teknik Borç / Kalite

- [ ] Flyway migration
- [ ] Unit testler
- [ ] Integration testler
- [ ] API contract testleri
- [x] CI workflow
- [ ] Dockerfile
- [ ] Kod formatlama / lint kuralları

## Gelecek Fikirler

- [ ] Gmail/e-posta ekstre analizi
- [ ] Akıllı ödeme önerileri
- [ ] Banka entegrasyonları mümkün oldukça adapter yapısı
- [ ] Çoklu para birimi
- [ ] Ortak hesap / aile paylaşımı
- [ ] CSV / Excel import-export
