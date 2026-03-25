# AppShopTour Database Schema

## Why We Start With A Small Schema

For learning purposes, the database should be simple enough to understand fully, but rich enough to support real product flows.

The first MVP schema is built around four tables:

- `users`
- `trips`
- `purchases`
- `purchase_items`

This is enough to support:

- authentication
- trip history
- active trip dashboard
- purchases list
- item creation

## Table 1: users

Purpose:

- stores account information
- owns all trips

Suggested columns:

- `id` UUID primary key
- `name` varchar not null
- `email` varchar not null unique
- `password_hash` varchar not null
- `preferred_currency` varchar not null default `'EUR'`
- `preferred_language` varchar not null default `'ru'`
- `theme_mode` varchar not null default `'dark'`
- `created_at` timestamp not null
- `updated_at` timestamp not null

Why these fields matter:

- `email` is the login identifier
- `password_hash` teaches proper auth storage
- preferences support the profile screen later

## Table 2: trips

Purpose:

- stores each shopping trip
- drives the home dashboard and trip history screens

Suggested columns:

- `id` UUID primary key
- `user_id` UUID not null references `users(id)`
- `title` varchar not null
- `country_code` varchar(2) not null
- `city_name` varchar null
- `start_date` date not null
- `end_date` date not null
- `status` varchar not null
- `budget_amount` decimal(12, 2) not null default 0
- `budget_currency` varchar(3) not null default `'EUR'`
- `created_at` timestamp not null
- `updated_at` timestamp not null

Recommended status values:

- `PLANNED`
- `ACTIVE`
- `COMPLETED`

Why totals are not stored here yet:

- in the beginning it is more educational to calculate totals from purchases and items
- later we can add cached aggregate columns if performance or convenience requires it

## Table 3: purchases

Purpose:

- stores shopping entries grouped by store
- powers the purchases list screen

Suggested columns:

- `id` UUID primary key
- `trip_id` UUID not null references `trips(id)`
- `store_name` varchar not null
- `store_address` varchar null
- `purchase_date` timestamp not null
- `currency` varchar(3) not null
- `vat_rate` decimal(5, 2) null
- `created_at` timestamp not null
- `updated_at` timestamp not null

Why `purchases` are separate from `purchase_items`:

- one store visit can contain several products
- the UI already suggests store-level grouping
- this separation teaches normalization and clean aggregation

## Table 4: purchase_items

Purpose:

- stores individual goods inside a purchase
- supports add-item flow and totals calculation

Suggested columns:

- `id` UUID primary key
- `purchase_id` UUID not null references `purchases(id)`
- `name` varchar not null
- `quantity` integer not null
- `unit_price` decimal(12, 2) not null
- `total_price` decimal(12, 2) not null
- `vat_included` boolean not null default true
- `vat_rate` decimal(5, 2) not null default 0
- `vat_amount` decimal(12, 2) not null default 0
- `image_url` varchar null
- `created_at` timestamp not null
- `updated_at` timestamp not null

Why `total_price` is stored:

- it makes list rendering and aggregation simpler
- it lets us compare calculated values against saved values during learning and testing

## Relationships

- `users 1 -> N trips`
- `trips 1 -> N purchases`
- `purchases 1 -> N purchase_items`

## How The Dashboard Is Calculated

The active trip dashboard can be derived with queries:

- total spent = sum of `purchase_items.total_price` for the active trip
- total items = sum of item quantities for the active trip
- total stores = count of purchases for the active trip
- budget progress = total spent / `trips.budget_amount`
- VAT total = sum of `purchase_items.vat_amount` for the active trip

This is useful for learning because it teaches:

- joins
- aggregate functions
- grouped queries

## Suggested Constraints

- `users.email` unique
- only one `ACTIVE` trip per user at a time enforced in service logic first
- `quantity > 0`
- `unit_price >= 0`
- `total_price >= 0`
- `vat_rate >= 0`
- `budget_amount >= 0`

## What We Intentionally Skip For Now

- receipts table
- currencies table
- exchange_rates table
- tax refund claims
- file storage metadata
- audit logs

These can come later. They are useful, but they would overload the first learning iteration.

## Learning Notes

This schema is good for training because:

1. every screen maps to concrete rows and relations
2. REST endpoints become obvious from table ownership
3. shared models can mirror the same concepts cleanly
4. the server can teach validation, transactions, and aggregates without too much complexity

## Next Step

After agreeing on this schema, the next implementation task should be:

1. create SQL migrations
2. add backend table models
3. implement the `GET /api/v1/trips/active` dashboard path
4. mirror the response model in `shared`
