# DataTable Component Refactoring Plan

## Current Issues

The current `DataTable.tsx` component has grown to over 1000 lines of code and suffers from several issues:

1. **Code Size**: Over 1000 lines, making it difficult to maintain
2. **Mixed Responsibilities**: Handles multiple concerns (filtering, sorting, pagination, data fetching, rendering)
3. **Complex State Management**: Contains numerous state variables with complex interactions
4. **Limited Reusability**: Functionality is tightly coupled, making it hard to reuse parts in other components

## Refactoring Strategy

### 1. Directory Structure

```
frontend/app/components/common/DataTable/
├── index.tsx               # Main component (exports the composed DataTable)
├── components/             # UI Components
│   ├── filters/
│   │   ├── TextFilter.tsx
│   │   ├── NumericFilter.tsx
│   │   ├── BooleanFilter.tsx
│   │   └── ColumnFilter.tsx
│   ├── Pagination.tsx
│   ├── TableHeader.tsx
│   ├── TableRow.tsx
│   ├── TableActions.tsx
│   ├── ExpandableDetail.tsx
│   └── ConfirmDialog.tsx
├── hooks/                  # Custom Hooks
│   ├── useTableData.tsx
│   ├── useTableSort.tsx
│   ├── useTableFilter.tsx
│   ├── useTablePagination.tsx
│   ├── useTableEdit.tsx
│   └── useTableSelection.tsx
└── types.ts               # Type definitions
```

### 2. Component Extraction

Extract the following components from the main file:

1. **Filter Components**
   - Move `TextFilter`, `NumericFilter`, `BooleanFilter`, and `ColumnFilter` to separate files
   - Create a filter factory that selects the appropriate filter type

2. **Pagination Component**
   - Already well-structured, move to its own file

3. **Table Header & Row Components**
   - Create reusable components for rendering headers and rows
   - Separate sorting UI from sorting logic

4. **Confirmation Dialogs**
   - Create a reusable confirmation dialog component for edit/save/delete actions

### 3. Custom Hooks

Extract logic into the following custom hooks:

#### `useTableData`

```typescript
function useTableData({
  initialData,
  entityType,
  fetchData: providedFetchData,
  search
}: TableDataHookProps) {
  const [data, setData] = useState<TableFetchResponse>(initialData || emptyTableData);
  const [isLoading, setIsLoading] = useState(false);
  
  // Data fetching logic from the original component
  
  return {
    data,
    isLoading,
    reload: loadData
  };
}
```

#### `useTableSort`

```typescript
function useTableSort({
  onSortChange
}: TableSortHookProps, reloadData: ReloadFunction) {
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  
  // Sorting logic from the original component
  
  return {
    sortField,
    sortDirection,
    handleSortChange: handleSort
  };
}
```

#### `useTableFilter`

```typescript
function useTableFilter({
  onFilterChange
}: TableFilterHookProps, reloadData: ReloadFunction) {
  const [activeFilters, setActiveFilters] = useState<Record<string, FilterState>>({});
  const [openFilterColumn, setOpenFilterColumn] = useState<string | null>(null);
  
  // Filtering logic from the original component
  
  return {
    filters: activeFilters,
    activeFilterKeys: columnsWithActiveFilters,
    openFilterColumn,
    handleFilterChange,
    handleFilterOpen: setOpenFilterColumn,
    handleClearFilters
  };
}
```

#### `useTablePagination`

```typescript
function useTablePagination({
  onPageChange
}: TablePaginationHookProps, data: TableFetchResponse, reloadData: ReloadFunction) {
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: 10,
    totalItems: data.totalElements,
    totalPages: Math.max(1, data.totalPage)
  });
  
  // Pagination logic from the original component
  
  return {
    pagination,
    handlePageChange,
    handlePageSizeChange
  };
}
```

#### `useTableEdit`

```typescript
function useTableEdit({
  onEdit,
  onDelete,
  onSave,
  entityType
}: TableEditHookProps, reloadData: ReloadFunction) {
  const [editingRowId, setEditingRowId] = useState<number | null>(null);
  const [isAddingNewRow, setIsAddingNewRow] = useState(false);
  const [editedData, setEditedData] = useState<TableRow | null>(null);
  
  // Edit/delete/save logic from the original component
  
  return {
    editingRowId,
    isAddingNewRow,
    handleEdit,
    handleSave,
    handleDelete,
    handleCancel,
    handleAddNew,
    confirmDialog
  };
}
```

### 4. Main Component Refactoring

The main `index.tsx` file will compose all the pieces:

```typescript
export default function DataTable(props: DataTableProps) {
  // Use hooks to manage state and logic
  const { data, isLoading, reload } = useTableData(props);
  const { sortField, sortDirection, handleSortChange } = useTableSort(props, reload);
  const { filters, activeFilterKeys, openFilterColumn, handleFilterChange, 
          handleFilterOpen } = useTableFilter(props, reload);
  const { pagination, handlePageChange, handlePageSizeChange } = 
          useTablePagination(props, data, reload);
  const { editingRowId, isAddingNewRow, handleEdit, handleSave, handleDelete, 
          handleCancel, handleAddNew, confirmDialog } = useTableEdit(props, reload);
  
  // Compose UI from smaller components
  return (
    <div className="w-full relative flex flex-col">
      <TableControls
        searchTerm={searchTerm}
        onSearchChange={handleSearchChange}
        activeFilterKeys={activeFilterKeys}
        filters={filters}
        columns={effectiveColumns}
        filterOptions={props.filterOptions}
        onFilterChange={handleFilterChange}
        tableActions={tableActions}
        addItemButton={actualAddItemButton}
        onAddNew={handleAddNew}
        isLoading={isLoading}
      />
      
      <div className="bg-[#1e1e1e] border border-[#3c3c3c] rounded-md overflow-hidden relative w-full flex-grow flex flex-col">
        <div className="overflow-x-auto w-full flex-1">
          <table className="w-full relative table-auto" aria-busy={isLoading} role="grid">
            <TableHeader
              columns={effectiveColumns}
              sortField={sortField}
              sortDirection={sortDirection}
              onSort={handleSortChange}
              filters={filters}
              openFilterColumn={openFilterColumn}
              onFilterChange={handleFilterChange}
              onFilterOpen={handleFilterOpen}
              showDetailView={props.showDetailView}
              showActions={props.showDefaultActions}
            />
            
            <TableBody
              data={data}
              columns={effectiveColumns} 
              isLoading={isLoading}
              expandedRowId={expandedRowId}
              editingRowId={editingRowId}
              isAddingNewRow={isAddingNewRow}
              newRowData={newRowData}
              actions={effectiveActions}
              showDetailView={props.showDetailView}
              showActions={props.showDefaultActions}
              onRowClick={handleRowClick}
              onEdit={handleEdit}
              onDelete={handleDelete}
              emptyMessage={props.emptyMessage}
              renderRowDetail={renderRowDetail}
            />
          </table>
          
          {data.rows.length > 0 && (
            <Pagination 
              pagination={pagination}
              onPageChange={handlePageChange}
              onPageSizeChange={handlePageSizeChange}
            />
          )}
        </div>
      </div>
      
      {/* Render confirmation dialogs */}
      {confirmDialog}
    </div>
  );
}
```

## Implementation Approach

### Phase 1: Extract Components Without Changing Functionality

1. Create the folder structure
2. Move filter components to separate files
3. Move pagination to its own file
4. Create confirmation dialog component

### Phase 2: Extract Logic to Custom Hooks

1. Implement the hooks listed above
2. Update the main component to use these hooks
3. Test to ensure functionality is preserved

### Phase 3: Complete Refactoring

1. Create remaining UI components
2. Update imports throughout the project
3. Write tests for individual components
4. Add documentation for each component and hook

## Benefits

1. **Improved Maintainability**: Each file has a single responsibility
2. **Better Testability**: Smaller components and isolated logic are easier to test
3. **Enhanced Reusability**: Components and hooks can be used in other parts of the application
4. **Simplified Understanding**: Developers can reason about individual pieces more easily
5. **Easier Future Enhancements**: Adding features to specific aspects becomes simpler

## Potential Challenges

1. **State Coordination**: Ensuring state is properly shared between hooks
2. **Prop Drilling**: May need context for deeply nested components
3. **Initial Refactor Effort**: Significant upfront work to break down the component

## Timeline

- **Week 1**: Extract components and set up folder structure (Phase 1)
- **Week 2**: Extract hooks and begin refactoring main component (Phase 2)
- **Week 3**: Complete refactoring, testing and documentation (Phase 3)
