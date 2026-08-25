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
- [ ] Service katmanı
- [ ] DTO / mapper yapısı
- [ ] CreditCard CRUD API
- [ ] Loan CRUD API
- [ ] Subscription CRUD API
- [ ] Bill CRUD API
- [ ] `GET /api/v1/payments/upcoming`
- [ ] `GET /api/v1/dashboard/summary`

## Sonraki

- [ ] Payment create/update/delete API
- [ ] Tekrarlayan ödeme oluşturma
- [ ] Kredi taksit planı üretme
- [ ] Ödeme tamamlandı işaretleme
- [ ] Geciken ödeme hesaplama
- [ ] Validation mesajları
- [x] Global exception handler altyapısı
- [ ] Swagger

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
- [ ] CI
- [ ] Dockerfile
- [ ] Kod formatlama / lint kuralları

## Gelecek Fikirler

- [ ] Gmail/e-posta ekstre analizi
- [ ] Akıllı ödeme önerileri
- [ ] Banka entegrasyonları mümkün oldukça adapter yapısı
- [ ] Çoklu para birimi
- [ ] Ortak hesap / aile paylaşımı
- [ ] CSV / Excel import-export
